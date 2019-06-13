import Vue from 'vue'
import VueRouter from 'vue-router'
import Collections from '../components/Collections'
Vue.use(VueRouter)

export default new VueRouter({
  routes: [
    {
      path: '/',
      name: 'Collections',
      component: Collections
    },
  ]
})
