package fakehttp

import (
	"encoding/binary"
	"github.com/xpwu/go-stream/conn"
	"sync"
	"sync/atomic"
)

type pushIDKey struct{}

type PushID struct {
	c     conn.Conn
	Value uint32
}

type pushIdValue uint32

func (p *pushIdValue) UnInit() {}

func (p *PushID) ToBytes() []byte {
	ret := make([]byte, 4)
	binary.BigEndian.PutUint32(ret, p.Value)
	return ret
}

func GetPushID(c conn.Conn) *PushID {
	addr := pushIdValue(1)
	value, _ := c.LoadOrStore(pushIDKey{}, &addr)
	newValue := atomic.AddUint32((*uint32)(value.(*pushIdValue)), 1)

	return &PushID{
		c:     c,
		Value: newValue,
	}
}

func Bytes2PushID(b []byte, c conn.Conn) *PushID {
	return &PushID{
		c:     c,
		Value: binary.BigEndian.Uint32(b),
	}
}

type waitQ sync.Map

func (w *waitQ) UnInit() {
	(*sync.Map)(w).Range(func(key, value interface{}) bool {
		close(value.(chan struct{}))
		return true
	})
}

func (w *waitQ) toMap() *sync.Map{
  return (*sync.Map)(w)
}

type waitQKey struct {}

func (p *PushID) WaitAck() <-chan struct{}{
  w := &waitQ{}
  n,_ := p.c.LoadOrStore(waitQKey{}, w)
  // 防止死锁, 1个buffer
  ret := make(chan struct{}, 1)
  n.(*waitQ).toMap().Store(p.Value, ret)
  return ret
}

func (p *PushID) Ack() {
  n,ok := p.c.Load(waitQKey{})
  if !ok {
    return
  }

  ch,ok := n.(*waitQ).toMap().LoadAndDelete(p.Value)
  if !ok {
    return
  }

  ch.(chan struct{}) <- struct{}{}
}

func (p *PushID) CancelWaitingAck() {
  n,ok := p.c.Load(waitQKey{})
  if !ok {
    return
  }

	ch,ok := n.(*waitQ).toMap().LoadAndDelete(p.Value)
	if ok {
		close(ch.(chan struct{}))
	}
}

