import Vue from 'vue'
import VueRouter from 'vue-router'
import Gallery from '../components/Gallery'

Vue.use(VueRouter)


export default new VueRouter({
  routes: [
    {
      path: '/',
      name: 'Gallery',
      component: Gallery
    },
  ]
})
