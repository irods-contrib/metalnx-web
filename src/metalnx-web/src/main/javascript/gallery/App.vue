<template>

  <div id="container">
    <router-view></router-view>
  </div>
</template>

<script>
  import Vue from 'vue'
  import BootstrapVue from 'bootstrap-vue'
  import App from './App'

  Vue.use(BootstrapVue)

  import 'bootstrap/dist/css/bootstrap.css'
  import 'bootstrap-vue/dist/bootstrap-vue.css'
  import router from './router'

  Vue.config.productionTip = false
  export default {
    router,
    //el: '#app'
  }
</script>
