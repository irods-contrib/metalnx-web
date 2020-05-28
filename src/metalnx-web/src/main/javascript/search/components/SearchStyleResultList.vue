<template>
  <div class="container mb-3">
     <div class="row">
       <h3><b-link v-bind:href="searchResultEntry.url_link" target="_blank">{{ searchResultEntry.title }}</b-link></h3>
      </div>
      <div class="row">
        <p><span v-html="searchResultEntry.content_text"></span></p> 
      </div>
      <div v-if="searchResultEntry.links.links.length > 0">
         <b-button variant="link" size="lg" v-on:click="isActive = !isActive" v-bind:id="searchResultEntry.title" v-bind:title="searchResultEntry.links.linkset_description">{{ searchResultEntry.links.linkset_title }} </b-button>
          <div v-if="isActive">
            <div v-for="item in searchResultEntry.links.links" v-bind:key="item.link_url">
              <h8><b-link v-bind:href="item.link_url" target="_blank">{{ item.link_text }}</b-link></h8>
            </div>
        </div>
      </div>
  </div>
</template>

<script>
  export default {
    data() {
    return {isActive: false}
    },
    name:'SearchStyleResultList',
    props:['searchResultEntry']
  }
</script>
