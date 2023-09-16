package conn

import (
	"context"
	"github.com/xpwu/go-log/log"
	"time"
)

func TryConcurrent(ctx context.Context, concurrent chan struct{}) {
	_, logger := log.WithCtx(ctx)
loop:
	for {
		select {
		case concurrent <- struct{}{}:
			break loop
		default:
			logger.Warning("wait concurrent")
			select {
			case concurrent <- struct{}{}:
				break loop
			case <-ctx.Done():
				break loop
			case <-time.After(5 * time.Second):
				logger.Warning("continue waiting concurrent")
			}
		}
	}
}

func DoneConcurrent(ctx context.Context, concurrent chan struct{}) {
	_, logger := log.WithCtx(ctx)
	// 防止调用TryConcurrent错误，造成堵塞，所以加了default
	select {
	case <-concurrent:
	default:
		logger.Error("<-concurrent error, maybe call 'TryConcurrent/DoneConcurrent' error. ")
	}
}
