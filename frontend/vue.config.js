  const { defineConfig } = require('@vue/cli-service');
  const { DefinePlugin } = require('webpack');

  module.exports = defineConfig({
    devServer: {
      port: 8082,
      proxy: {
        '/api': {
          target: 'http://localhost:8081', // 后端服务器地址
          changeOrigin: true,
          pathRewrite: {
            '^/api': '' // 移除/api前缀
          }
        }
      }
    },
    transpileDependencies: true,
    publicPath: process.env.NODE_ENV === 'production' ? '/myapp/' : '/',
    configureWebpack: {
      plugins: [
        new DefinePlugin({
          __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false)
        }),
      ],
    },
  });