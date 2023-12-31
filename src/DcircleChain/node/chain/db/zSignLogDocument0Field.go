package db

// ---- auto generated by builder.go, NOT modify this file ----

import (
	field "github.com/xpwu/go-db-mongo/mongodb/field"
)

type SignLogDocument0FieldUpdaterF struct {
	*baseSignLogDocument0Field
	*field.StructUpdaterF
}

func (s *SignLogDocument0FieldUpdaterF) FullName() string {
	return s.name
}

type SignLogDocument0FieldFilterF struct {
	*baseSignLogDocument0Field
	*field.StructFilterF
}

func (s *SignLogDocument0FieldFilterF) FullName() string {
	return s.name
}

type SignLogDocument0Field struct {
	*baseSignLogDocument0Field
	SignLogDocument0FieldUpdaterF *SignLogDocument0FieldUpdaterF
	SignLogDocument0FieldFilterF  *SignLogDocument0FieldFilterF
}

func NewSignLogDocument0Field(fName string) *SignLogDocument0Field {
	base := &baseSignLogDocument0Field{fName}
	// 没有name时，不能做updater与filter操作，比如最顶层的Struct
	if fName == "" {
		return &SignLogDocument0Field{baseSignLogDocument0Field: base}
	}
	up := &SignLogDocument0FieldUpdaterF{base, field.NewStructUpdaterF(fName)}
	fl := &SignLogDocument0FieldFilterF{base, field.NewStructFilterF(fName)}

	return &SignLogDocument0Field{base, up, fl}
}

// 对应于 bson struct 中的 inline 修饰符
func NewSignLogDocument0FieldInline(fName string) *SignLogDocument0Field {
	return &SignLogDocument0Field{baseSignLogDocument0Field: &baseSignLogDocument0Field{fName}}
}

func (s *SignLogDocument0Field) FullName() string {
	return s.name
}

type baseSignLogDocument0Field struct {
	name string
}

func (s *baseSignLogDocument0Field) SignHash() *field.String0F {
	n := field.StructNext(s.name, "_id")
	return field.NewString0F(n)
}

func (s *baseSignLogDocument0Field) ReqId() *field.String0F {
	n := field.StructNext(s.name, "ReqId")
	return field.NewString0F(n)
}

func (s *baseSignLogDocument0Field) Status() *field.Int0F {
	n := field.StructNext(s.name, "Status")
	return field.NewInt0F(n)
}

func (s *baseSignLogDocument0Field) FailReason() *field.String0F {
	n := field.StructNext(s.name, "FailReason")
	return field.NewString0F(n)
}

func (s *baseSignLogDocument0Field) CreateTime() *field.Uint640F {
	n := field.StructNext(s.name, "CreateTime")
	return field.NewUint640F(n)
}

func (s *baseSignLogDocument0Field) Data() *field.String0F {
	n := field.StructNext(s.name, "Data")
	return field.NewString0F(n)
}
