<template>
  <div class="container">
     <div class="row">
       <h3><b-link v-bind:href="searchResultEntry.url_link" target="_blank">{{ searchResultEntry.title }}</b-link></h3>
      </div>
      <div class="row">
        {{searchResultEntry.content_text}}
      </div>
    <b-button variant="link" v-on:click="isActive = !isActive">Similar articles</b-button>
    <div v-if="isActive">
      <div v-for="item in searchResultEntry.links.links" v-bind:key="item.link_url">
          <h8><b-link v-bind:href="item.link_url" target="_blank">{{ item.link_text }}</b-link></h8>
        </div>
      </div>
  </div>
</template>

<script>
  export default {
    data() {
    return {isActive: false}
    },
    name:'SearchStyleResultEntry',
    props:['searchResultEntry']
  }
</script>
