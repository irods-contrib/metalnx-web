// vue.config.js
module.exports = {
  outputDir: 'target/dist/static',
  filenameHashing: false,
  publicPath: '/metalnx/',
  devServer: {
	  port:8888,
    proxy: {
      '/metalnx': {
        target: 'http://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  },
  pages: {
   collections: {
     entry: 'src/main/javascript/collections/main.js'
   },
   home: {
     entry: 'src/main/javascript/home/main.js'
   },
   notifications: {
     entry: 'src/main/javascript/notifications/main.js'
   }
 }
}
