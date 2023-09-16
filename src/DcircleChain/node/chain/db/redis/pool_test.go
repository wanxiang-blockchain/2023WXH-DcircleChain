package redis

import (
	"context"
	"testing"
)

func TestPush(t *testing.T) {
	pool := NewHashPool(context.Background())
	err1 := pool.TransactionLPush("Transaction_hash1")
	if err1 != nil {
		t.Error(err1)
	}
	err2 := pool.TransactionLPush("Transaction_hash2")
	if err2 != nil {
		t.Error(err2)
	}

	err4 := pool.ContentLPush("ContentLPush_hash1")
	if err4 != nil {
		t.Error(err4)
	}
	err3 := pool.ContentLPush("ContentLPush_hash2")
	if err3 != nil {
		t.Error(err3)
	}
}

func TestPop(t *testing.T) {
	pool := NewHashPool(context.Background())
	value, err := pool.ContentPop()
	if err != nil {
		t.Error(err)
		return
	}
	t.Logf("ContentPop %s \n", value)
}
