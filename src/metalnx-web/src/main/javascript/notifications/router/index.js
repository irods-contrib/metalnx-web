import Vue from 'vue'
import VueRouter from 'vue-router'
import Notifications from '../components/Notifications'
Vue.use(VueRouter)

export default new VueRouter({
  routes: [
    {
      path: '/',
      name: 'Notifications',
      component: Notifications
    },
  ]
})
