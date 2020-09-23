<template>
<div class="container">
    <h1>shoppingCart</h1>
</div>
</template>

<script>
import axios from 'axios'
export default {
  name: 'ShoppingCart',
  data(){
    cartList: ''
  },
  created: function () {  
    axios.get('/metalnx/api/shoppingCart/getCart/').then(response => (this.cartList = response.data.cartList))
    console.log('This is from vue cart app')
  },
}
</script>
<style>
</style>