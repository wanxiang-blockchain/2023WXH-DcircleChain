package fakehttp

import (
	"encoding/binary"
	"errors"
	"fmt"
	"github.com/xpwu/go-stream/conn"
	"net"
	"strings"
)

/**

content protocol:
     request ---
       reqid | headers | header-end-flag | data
         reqid: 4 bytes, net order;
         headers: < key-len | key | value-len | value > ... ;  [optional]
           key-len: 1 byte,  key-len = sizeof(key);
           value-len: 1 byte, value-len = sizeof(value);
         header-end-flag: 1 byte, === 0;
         data:       [optional]

		reqid = 1: client push ack to server.
					ack: no headers;
					data: pushId. 4 bytes, net order;

  ---------------------------------------------------------------------
     response ---
       reqid | status | data
         reqid: 4 bytes, net order;
         status: 1 byte, 0---success, 1---failed
         data: if status==success, data=<app data>    [optional]
               if status==failed, data=<error reason>


     reqid = 1: server push to client
				status: 0
				data: first 4 bytes --- pushId, net order;
							last --- real data

*/

/**
支持变量：
1、通过其他方式注册的变量，本方式在代码中暂时没有使用；
2、headers中key对应的值。比如客户端传递 "api"=>"sum"，对于配置文件中的变量 ${fhttp_api}将被替换为 "sum"
*/

const (
	ReqIDLen = 4
)

type Request struct {
	Conn   conn.Conn
	ReqId  []byte
	Header map[string]string
	Data   []byte
}

var requestPErr = errors.New("stream request protocol error")

func NewRequest(conn conn.Conn, data []byte) (req *Request, err error) {
	req = &Request{
		Conn:   conn,
		Header: make(map[string]string),
	}

	// must have reqid + header-end-flag
	if len(data) < 5 {
		return nil, fmt.Errorf("error fakehttp request. the len of data must be more than 5, but be %d",
			len(data))
	}

	start := 0

	req.ReqId = data[start:4]
	start += 4

	for start < len(data) && data[start] != 0 {
		keyLen := int(data[start])
		start += 1
		key := string(data[start : start+keyLen])
		start += keyLen
		valLen := int(data[start])
		start += 1
		val := string(data[start : start+valLen])
		start += valLen

		req.Header[key] = val
	}

	// header-end-flag
	start += 1

	if start == len(data) {
		req.Data = make([]byte, 0, 0)
		return
	}

	if start > len(data) {
		err = requestPErr
		return
	}

	req.Data = data[start:]
	return
}

func (r *Request) GetVar(name string) string {
	if ret, ok := getVarValue(name, r); ok {
		return ret
	}

	const prefix = "fhttp_"
	if strings.HasPrefix(name, prefix) {
		name = strings.TrimPrefix(name, prefix)
		if name == "reqid" {
			return fmt.Sprintf("%d", r.GetReqId())
		}

		if ret, ok := r.Header[name]; ok {
			return ret
		}
	}

	return r.Conn.GetVar(name)
}

//func (r *Request) SetVar(name string, value string) {
// // nothing to do
//}

func (r *Request) GetReqId() uint32 {
	return binary.BigEndian.Uint32(r.ReqId)
}

func (r *Request) SetReqId(req uint32) {
	r.ReqId = make([]byte, ReqIDLen)
	binary.BigEndian.PutUint32(r.ReqId, req)
}

func (r *Request) Buffers() net.Buffers {
	ret := make(net.Buffers, 1)
	ret[0] = r.ReqId
	for k, v := range r.Header {
		kl := byte(len(k))
		vl := byte(len(v))
		t := make([]byte, 2+kl+vl)
		ret = append(ret, t)
		t[0] = kl
		t = t[1:]
		copy(t, k)
		t[kl] = vl
		t = t[kl+1:]
		copy(t, v)
	}

	// header-end-flag and data
	ret = append(ret, []byte{0}, r.Data)

	return ret
}

func (r *Request) IsPushAck() (yes bool, err error) {
	if r.GetReqId() != 1 {
		return false, nil
	}

	// no header; data must be push id
	if len(r.Header) + len(r.Data)  != 4 {
		return false, requestPErr
	}

	return true, nil
}

var (
	varMap = make(map[string]func(request *Request) string)
)

func RegisterVar(name string, value func(request *Request) string) {
	varMap[name] = value
}

func getVarValue(name string, request *Request) (value string, ok bool) {
	f, ok := varMap[name]
	if !ok {
		return
	}

	return f(request), ok
}
