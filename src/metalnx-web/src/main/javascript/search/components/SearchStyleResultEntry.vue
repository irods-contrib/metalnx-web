<template>
  <div class="container">
     <div class="row">
       <h3><b-link v-bind:href="searchResultEntry.url_link" target="_blank">{{ searchResultEntry.title }}</b-link></h3>
      </div>
      <div class="row">
        {{searchResultEntry.content_text}}
      </div>
     
  </div>
</template>
<script>
  export default {
    name:'SearchStyleResultEntry',
    props:['searchResultEntry']
  }
</script>
