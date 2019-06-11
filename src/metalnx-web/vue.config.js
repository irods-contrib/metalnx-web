// vue.config.js
module.exports = {
  outputDir: 'target/dist/static',
  filenameHashing: false,
  publicPath: '/metalnx/static/',
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
