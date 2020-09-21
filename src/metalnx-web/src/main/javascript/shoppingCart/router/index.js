import Vue from 'vue'
import VueRouter from 'vue-router'
import ShoppingCart from '../components/ShoppingCart'

Vue.use(VueRouter)


export default new VueRouter({
  routes: [
    {
      path: '/',
      name: 'ShoppingCart',
      component: ShoppingCart
    },
  ]
})
