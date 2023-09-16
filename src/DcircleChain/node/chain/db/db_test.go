package db

import (
	"fmt"
	"github.com/xpwu/go-db-mongo/mongodb/field"
	"reflect"
)

func ExampleSignInfo() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&SignDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleNonceInfo() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&NonceDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleSignLog() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&SignLogDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleAccount() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&AccountDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleReceipt() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&ReceiptDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleBlock() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&BlockDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleBlockNumber() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&BlockNumberDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleAnchoring() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&AnchoringDocument{}))
	fmt.Print(true)
	// Output:
	// true
}

func ExampleTransaction() {
	builder := field.New()
	builder.Build(reflect.TypeOf(&TransactionDocument{}))
	fmt.Print(true)
	// Output:
	// true
}
