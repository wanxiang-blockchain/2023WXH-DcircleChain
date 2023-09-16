package conn

import (
	"context"
	"github.com/xpwu/go-var/vari"
	"github.com/xpwu/go-xnet/connid"
	"net"
	"sync"
)

type ExtraValue interface {
	UnInit()
}

// 并发安全
type extraValueInterface interface {
	Load(interface{}) (ExtraValue, bool)
	Store(key interface{}, value ExtraValue)
	LoadOrStore(key interface{}, value ExtraValue) (actual ExtraValue, loaded bool)
	LoadAndDelete(key interface{}) (value ExtraValue, loaded bool)
	Delete(interface{})
	Range(func(key interface{}, value ExtraValue) (shouldContinue bool))
}

type Base sync.Map

func (b *Base) Load(i interface{}) (ExtraValue, bool) {
	v,ok := ((*sync.Map)(b)).Load(i)
	return v.(ExtraValue), ok
}

func (b *Base) Store(key interface{}, value ExtraValue) {
	((*sync.Map)(b)).Store(key, value)
}

func (b *Base) LoadOrStore(key interface{}, value ExtraValue) (actual ExtraValue, loaded bool) {
	v,ok := ((*sync.Map)(b)).LoadOrStore(key, value)
	return v.(ExtraValue), ok
}

func (b *Base) LoadAndDelete(key interface{}) (value ExtraValue, loaded bool) {
	v,ok := ((*sync.Map)(b)).LoadAndDelete(key)
	return v.(ExtraValue), ok
}

func (b *Base) Delete(i interface{}) {
	((*sync.Map)(b)).Delete(i)
}

func (b *Base) Range(f func(key interface{}, value ExtraValue) (shouldContinue bool)) {
	f1 := func(key interface{}, value interface{}) (shouldContinue bool) {
		return f(key, value.(ExtraValue))
	}
	((*sync.Map)(b)).Range(f1)
}

func (b *Base) Close() {
	b.Range(func(key interface{}, value ExtraValue) (shouldContinue bool) {
		value.UnInit()
		return true
	})
}

type Conn interface {
	extraValueInterface
	vari.VarObject
	Id() connid.Id
	// 所有的实现中，需要满足 multiple goroutines 的同时调用
	Write(buffers net.Buffers) error

	// 所有的写将中断，并返回错误
	CloseWith(err error)

	Context() context.Context
}

var (
	connMap = &sync.Map{}
)

func AddConn(conn Conn) {
	connMap.Store(conn.Id(), conn)
}

func DelConn(conn Conn) {
	connMap.Delete(conn.Id())
}

func GetConn(id connid.Id) (conn Conn, ok bool) {
	res, ok := connMap.Load(id)
	if ok {
		conn = res.(Conn)
	}

	return
}

var (
	varMap = make(map[string]func(conn Conn) string)
	mu     sync.RWMutex
)

// 静态注册，不能在服务过程中再注册
func RegisterVar(name string, value func(conn Conn) string) {
	mu.Lock()
	varMap[name] = value
	mu.Unlock()
}

func GetVarValue(name string, conn Conn) (value string, ok bool) {
	mu.RLock()
	f, ok := varMap[name]
	mu.RUnlock()

	if !ok {
		return
	}

	return f(conn), ok
}
