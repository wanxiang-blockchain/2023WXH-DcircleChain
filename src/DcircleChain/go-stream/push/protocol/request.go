package protocol

import (
	"encoding/binary"
	"errors"
	"github.com/xpwu/go-stream/push/core"
	"github.com/xpwu/go-xnet/xtcp"
	"io"
	"time"
)

/**
Request:
   sequence | token | subprotocol | len | <data>
     sizeof(sequence) = 4. net order
     sizeof(token) = 32 . hex
     sizeof(subprotocol) = 1.
     sizeof(len) = 4. len = sizof(data) net order
     data: subprotocol Request data

  Response:
   sequence | state | len | <data>
     sizeof(sequence) = 4. net order
     sizeof(state) = 1.
               state = 0: success; 1: hostname error
                ; 2: token not exist; 3: server intelnal error
     sizeof(len) = 4. len = sizeof(data) net order
     data: subprotocol Response data
*/

const SequenceLen = 4

type Request struct {
	Conn        *xtcp.Conn
	Sequence    []byte
	Token       []byte
	SubProtocol byte
	Data        []byte
}

func NewRequest(conn *xtcp.Conn) *Request {
	return &Request{Conn: conn, Data: make([]byte, 0)}
}

func (r *Request) Read(until time.Time) (err error) {
	err = r.Conn.SetReadDeadline(until)
	if err != nil {
		return
	}

	b := make([]byte, SequenceLen+core.TokenLen+1+4)

	_, err = io.ReadFull(r.Conn, b)
	if err != nil {
		return err
	}

	r.Sequence = b[0:4]
	b = b[4:]

	r.Token = b[:core.TokenLen]
	b = b[core.TokenLen:]

	r.SubProtocol = b[0]
	b = b[1:]

	// 控制大小  默认1M
	length := binary.BigEndian.Uint32(b)
	if length > 1*1024*1024 {
		return errors.New("data's length must be less than 1MB")
	}

	r.Data = make([]byte, length)
	err = r.Conn.SetReadDeadline(time.Now().Add(5 * time.Second))
	if err != nil {
		return
	}
	if _, err = io.ReadFull(r.Conn, r.Data); err != nil {
		return err
	}

	return
}

func (r *Request) SetSequence(s uint32) {
	r.Sequence = make([]byte, SequenceLen)
	binary.BigEndian.PutUint32(r.Sequence, s)
}

func (r *Request) GetSequence() uint32 {
	return binary.BigEndian.Uint32(r.Sequence)
}

func (r *Request) Write() (err error) {
	buffer := make([][]byte, 4)
	buffer[0] = r.Sequence
	buffer[1] = r.Token
	buffer[2] = make([]byte, 5)
	buffer[2][0] = r.SubProtocol
	binary.BigEndian.PutUint32(buffer[2][1:], uint32(len(r.Data)))
	buffer[3] = r.Data

	_, err = r.Conn.WriteBuffers(buffer)
	return
}
