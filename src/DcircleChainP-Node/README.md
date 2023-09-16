# DcircleChainP-Node

## 项目相关

### 项目介绍：

    DcircleChain P-Node 是用户部署在Dcircle Chain上的节点行为及DID资产管理端口，也是Dcircle生态所有非金融应用场景的授权接入端口。

### 目录结构：

    ├── app/                  # 项目入口
    ├── baseui/               # 公共样式组件
    ├── deapps/               # Dcircle生态应用入口
    ├── did/                  # DID身份页面
    ├── foundation/           # 核心组件
        ├──── api/            # 数据接口
        ├──── base/           # 基础UI组件
            ├──── AppRouter   # 页面路由
        ├──── db/             # 数据库
        ├──── oss/            # 云存储oss相关文件
        ├──── utils/          # 通用工具类
        ├──── ...             # 其余文件为具体业务文件
    ├── image_preview/        # 加密图片预览
    ├── ktdbtable/            # 可自动升级的数据库组件
    ├── login/                # 身份认证页面
    ├── node/                 # Dcircle节点入口页面
    ├── stream/               # Server通讯
    ├── .gitignore            # Git忽略文件配置
    ├── build.gradle          # 项目配置文件，包含依赖和脚本命令


### Client Structure

1. 使用事件驱动型，让数据模型异步化流转，实时表达链上共识状态，让用户像 Web2 一样低门槛体验 Web3 产品的交互。
2. 适用于 iOS、Android、Web 等各种 DeApp 环境，提高开发效率，降低开发成本。

![Client Structure.jpg](..%2F..%2Fresource%2FClient%20Structure.jpg)

### Code Framework
![DcircleChainP-Node.jpg](..%2F..%2Fresource%2FDcircleChainP-Node.jpg)

