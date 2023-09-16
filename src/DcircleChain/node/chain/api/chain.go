package api

import (
	"github.com/xpwu/go-api-token/tapi"
	"github.com/xpwu/go-tinyserver/api"
)

type Suite struct {
	tapi.PostJsonLoginAPI
}

func (s *Suite) MappingPreUri() string {
	return "/chain/chain"
}

func AddAPI() {
	api.Add(func() api.Suite {
		return &Suite{}
	})
}
