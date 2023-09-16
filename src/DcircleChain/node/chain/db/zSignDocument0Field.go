package db

// ---- auto generated by builder.go, NOT modify this file ----

import (
	field "github.com/xpwu/go-db-mongo/mongodb/field"
)

type SignDocument0FieldUpdaterF struct {
	*baseSignDocument0Field
	*field.StructUpdaterF
}

func (s *SignDocument0FieldUpdaterF) FullName() string {
	return s.name
}

type SignDocument0FieldFilterF struct {
	*baseSignDocument0Field
	*field.StructFilterF
}

func (s *SignDocument0FieldFilterF) FullName() string {
	return s.name
}

type SignDocument0Field struct {
	*baseSignDocument0Field
	SignDocument0FieldUpdaterF *SignDocument0FieldUpdaterF
	SignDocument0FieldFilterF  *SignDocument0FieldFilterF
}

func NewSignDocument0Field(fName string) *SignDocument0Field {
	base := &baseSignDocument0Field{fName}
	// 没有name时，不能做updater与filter操作，比如最顶层的Struct
	if fName == "" {
		return &SignDocument0Field{baseSignDocument0Field: base}
	}
	up := &SignDocument0FieldUpdaterF{base, field.NewStructUpdaterF(fName)}
	fl := &SignDocument0FieldFilterF{base, field.NewStructFilterF(fName)}

	return &SignDocument0Field{base, up, fl}
}

// NewSignDocument0FieldInline 对应于 bson struct 中的 inline 修饰符
func NewSignDocument0FieldInline(fName string) *SignDocument0Field {
	return &SignDocument0Field{baseSignDocument0Field: &baseSignDocument0Field{fName}}
}

func (s *SignDocument0Field) FullName() string {
	return s.name
}

type baseSignDocument0Field struct {
	name string
}

func (s *baseSignDocument0Field) TxID() *field.String0F {
	n := field.StructNext(s.name, "_id")
	return field.NewString0F(n)
}

func (s *baseSignDocument0Field) Nonce() *field.Uint640F {
	n := field.StructNext(s.name, "Nonce")
	return field.NewUint640F(n)
}

func (s *baseSignDocument0Field) From() *field.String0F {
	n := field.StructNext(s.name, "From")
	return field.NewString0F(n)
}

func (s *baseSignDocument0Field) To() *field.String0F {
	n := field.StructNext(s.name, "To")
	return field.NewString0F(n)
}

func (s *baseSignDocument0Field) OpCode() *field.Uint80F {
	n := field.StructNext(s.name, "OpCode")
	return field.NewUint80F(n)
}

func (s *baseSignDocument0Field) SignTime() *field.Uint640F {
	n := field.StructNext(s.name, "SignTime")
	return field.NewUint640F(n)
}

func (s *baseSignDocument0Field) Payload() *field.Uint81Field {
	n := field.StructNext(s.name, "Payload")
	return field.NewUint81Field(n)
}

func (s *baseSignDocument0Field) Message() *field.Uint81Field {
	n := field.StructNext(s.name, "Message")
	return field.NewUint81Field(n)
}

func (s *baseSignDocument0Field) Sign() *field.Uint81Field {
	n := field.StructNext(s.name, "Sign")
	return field.NewUint81Field(n)
}