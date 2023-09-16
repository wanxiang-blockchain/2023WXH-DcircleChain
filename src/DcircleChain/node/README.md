**Dcircle Chain Node**  
## 项目相关 
#### 项目介绍
```
Dcircle Chain Node是Dcircle Chain生态的一个节点Node,负责节点数据的打包、验证、锚定以及和 L1/L2 合约的交互
```
#### 目录结构
```azure
├── README.md
├── chain
│   ├── api                            # Dcircle Chain 相关的接口
│   ├── db                             # Dcircle Chain 相关的数据存储
│   ├── eth                            # eth 相关的功能代码
│   │   ├── contracts                  # 和合约相关的交互代码
│   │   └── functions.go               # eth 相关的工具函数
│   ├── server                         # 节点服务相关的代码
│   └── sign                           # 签名相关的代码
├── go.mod                             # golang 工程的依赖配置文件
├── go.sum                             # golang 工程的依赖版本锁定文件
└── main.go                            # Dcircle Chain Node 入口 文件

```
