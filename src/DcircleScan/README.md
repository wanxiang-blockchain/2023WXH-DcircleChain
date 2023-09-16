## 一、安装依赖

`npm install or yarn install`

## 二、运行项目

`npm start or yarn start`

注意： 如果遇到 *3000* 端口号被占用的情况，以实际使用的端口号为准

## 三、打包发布

`npm run build or yarn build`

## 四、项目相关

### 项目介绍：
    
    DcircleScan 是Dcircle Chain （L3）区块链浏览器，透明公开可访问，支持用户在链上对节点行为、交易、验证进行实时查询追溯，以确保用户身份抽象数据在Dircle生态系列应用中流通的可用性和可证明性。

### 目录结构：

    ├── build/                # 打包后生成的文件(npm run build执行后自动生成)
    ├── node_modules/         # 依赖包（自动生成，不需要手动编辑）
    ├── public/               # 公共文件夹，存放静态资源
        ├──── index.html      # HTML模板文件
        ├──── favicon.ico     # 网站图标
    ├── src/                  # 项目源代码
        ├──── 3rdpart/        # 该项目使用的插件
        ├──── api/            # 接口网络请求
        ├──── db/             # IndexedDB数据库的数据表目录
        ├──── helper/         # 可复用的工具目录
        ├──── images/         # 图片资源
        ├──── languange/      # 多语言
        ├──── oss/            # 存储跟使用oss相关文件
        ├──── pages/          # 业务源码文件夹
           ├──── component/   # React组件
           ├──── cell/        # 消息类型相关
           ├──── iconfont/    # iconfont字体文件
           ├──── ...          # 其余文件为具体业务文件
        ├── App.tsx           # 主应用组件
        ├── index.tsx         # 入口文件，渲染主应用组件
    ├── .gitignore            # Git忽略文件配置
    ├── package.json          # 项目配置文件，包含依赖和脚本命令
    
    
### Code Framework
![DcircleScan.jpg](..%2F..%2Fresource%2FDcircleScan.jpg)