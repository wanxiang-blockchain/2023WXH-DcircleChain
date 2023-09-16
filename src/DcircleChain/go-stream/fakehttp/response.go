package fakehttp

import (
	"encoding/binary"
	"github.com/xpwu/go-stream/conn"
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

const (
	Success   byte   = 0
	Failed    byte   = 1
	PushReqId uint32 = 1
)

type Response struct {
	request *Request
	conn    conn.Conn
	reqId   []byte
	status  byte
	data    []byte
}

func pushReqId() []byte {
	ret := make([]byte, 4)
	binary.BigEndian.PutUint32(ret, PushReqId)

	return ret
}

func NewResponseWithPush(conn conn.Conn, pushId []byte, data []byte) *Response {
	return &Response{
		conn:   conn,
		reqId:  pushReqId(),
		data:   append(pushId, data...),
		status: Success,
	}
}

func NewResponseWithSuccess(request *Request, data []byte) *Response {
	return &Response{
		conn:    request.Conn,
		reqId:   request.ReqId,
		request: request,
		data:    data,
		status:  Success,
	}
}

func NewResponseWithFailed(request *Request, err error) *Response {

	return &Response{
		conn:    request.Conn,
		reqId:   request.ReqId,
		request: request,
		data:    []byte(err.Error()),
		status:  Failed,
	}
}

func NewResponseWithStream(data []byte) *Response {
	res := &Response{}
	res.reqId = data[:ReqIDLen]
	res.status = data[ReqIDLen]
	res.data = data[ReqIDLen+1:]

	return res
}

func (r *Response) ReqId() uint32 {
	return binary.BigEndian.Uint32(r.reqId)
}

func (r *Response) Status() byte {
	return r.status
}

func (r *Response) Data() []byte {
	return r.data
}

func (r *Response) Write() error {
	buffer := make([][]byte, 3, 3)
	buffer[0] = r.reqId
	st := make([]byte, 1, 1)
	st[0] = r.status
	buffer[1] = st
	buffer[2] = r.data

	return r.conn.Write(buffer)
}
