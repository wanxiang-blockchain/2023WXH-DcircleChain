const TerserPlugin = require('terser-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const path = require('path');
const child_process = require("child_process");

module.exports = function override(config, env) {
  config.optimization = {
    ...config.optimization,
    splitChunks: {
      cacheGroups: {
        echarts: {
          test: /[\\/]node_modules[\\/]echarts[\\/]/, // 匹配 ECharts 模块
          name: 'echarts', // 生成的文件名
          chunks: 'all', // 对所有的 chunks 进行代码分割
        }
      },
    },
  };

  // config.externals = {
  //   echarts: 'echarts' // 'echarts' 是全局变量的名称
  // };
    const gitVersion = child_process.execSync('git rev-parse --short HEAD').toString().trim();
    const prePath = `${gitVersion}`
    // const prePath = `static`
    const staticPath = `prePath`
    // 更改输出目录
    config.output.path = path.resolve(__dirname, `build/`);
    // js chunk asset
    config.output.filename = `${prePath}/js/[name].[contenthash:8].js`;
    config.output.chunkFilename = `${prePath}/js/[name].[contenthash:8].chunk.js`;
    config.output.assetModuleFilename = `${prePath}/media/[name].[hash][ext]`;
    if (process.env.BUILD_ENV == "prod") {
      // 更改公共路径
      config.output.publicPath = `https://didbrowser-prod.oss-cn-chengdu.aliyuncs.com/`
    }
    if (process.env.BUILD_ENV == "dev") {
      config.output.publicPath = `https://didbrowser-dev.oss-cn-chengdu.aliyuncs.com/`
    }

    if (process.env.BUILD_ENV == "beta") {
      config.output.publicPath = `https://didbrowser-beta.oss-cn-chengdu.aliyuncs.com/`
    }

    if (process.env.BUILD_ENV == "alpha") {
      config.output.publicPath = `https://didbrowser-alpha.oss-cn-chengdu.aliyuncs.com/`
    }

    const miniCssExtractPlugin = config.plugins.find(
        plugin => plugin instanceof MiniCssExtractPlugin
    );

    if (miniCssExtractPlugin) {
      // 更改 CSS 的输出路径
      miniCssExtractPlugin.options.filename = `${prePath}/css/[name].[contenthash:8].css`;
      miniCssExtractPlugin.options.chunkFilename = `${prePath}/css/[name].[contenthash:8].chunk.css`;
    }

  config.optimization.minimizer.push(
    new TerserPlugin({
      terserOptions: {
        compress: {
          drop_console: true,
        },
        output: {
          comments: false,
        },
      },
    })
  );
  return config;
};
