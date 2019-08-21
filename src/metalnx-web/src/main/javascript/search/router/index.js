import Vue from 'vue'
import VueRouter from 'vue-router'
import Search from '../components/Search'

Vue.use(VueRouter)


export default new VueRouter({
  routes: [
    {
      path: '/',
      name: 'Search',
      component: Search
    },
  ]
})
