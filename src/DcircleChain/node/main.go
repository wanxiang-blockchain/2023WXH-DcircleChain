package main

import (
	"dcircleserver/chain/api"
	"dcircleserver/chain/server"
	"fmt"
	"github.com/xpwu/go-cmd/arg"
	"github.com/xpwu/go-cmd/cmd"
	"github.com/xpwu/go-cmd/exe"
	"github.com/xpwu/go-config/configs"
	"github.com/xpwu/go-tinyserver/http"
	"os"
	"path/filepath"
)

func main() {
	cmd.RegisterCmd(cmd.DefaultCmdName, "start server", func(args *arg.Arg) {
		arg.ReadConfig(args)
		args.Parse()

		api.Start()
		server.Start()
		http.Start()

		block := make(chan struct{})
		<-block
	})

	argR := "config.json.default"
	cmd.RegisterCmd("config", "print config with json", func(args *arg.Arg) {
		args.String(&argR, "c", "the file name of config file")
		args.Parse()
		if !filepath.IsAbs(argR) {
			argR = filepath.Join(exe.Exe.AbsDir, argR)
		}
		configs.SetConfigurator(&configs.JsonConfig{PrintFile: argR})
		err := configs.Print()
		if err != nil {
			fmt.Println(err)
			os.Exit(-1)
		}
	})
	cmd.Run()
}
