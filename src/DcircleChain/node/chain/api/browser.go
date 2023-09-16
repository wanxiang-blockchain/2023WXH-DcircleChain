package api

import (
	login "dcircleserver/src"
	"github.com/xpwu/go-tinyserver/api"
)

type suite struct {
	login.PostNoTokenJsonAPI
}

func (s *suite) MappingPreUri() string {
	return "/browser"
}

func AddAPI() {
	api.Add(func() api.Suite {
		return &suite{}
	})
}
