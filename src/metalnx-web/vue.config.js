// vue.config.js
module.exports = {
  outputDir: 'target/dist/static',
  filenameHashing: false,
  publicPath: '/metalnx/',
  devServer: {
	  port:8888,
    proxy: {
      '/metalnx/api/': {
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
   search: {
     entry: 'src/main/javascript/search/main.js'
   },
   notifications: {
     entry: 'src/main/javascript/notifications/main.js'
   },
   gallery: {
     entry: 'src/main/javascript/gallery/main.js'
   }
 }
}
