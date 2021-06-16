import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue'
import App from './App'

Vue.use(BootstrapVue)

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false

// refactor to a properties

//axios.defaults.baseURL = "http://localhost:8888/v2"
//axios.defaults.headers.post['Access-Control-Allow-Origin'] = '*';

new Vue({
  render: h => h(App),
  
}).$mount('#app')