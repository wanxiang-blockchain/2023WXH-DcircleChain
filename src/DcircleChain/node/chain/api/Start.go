package api

import (
	"dcircleserver/chain/api/browser"
	"dcircleserver/chain/api/chain"
)

func Start() {
	browser.AddAPI()
	chain.AddAPI()
}
