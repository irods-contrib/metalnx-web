<template>
  <div class="container mb-3">
    <h4> Showing {{ (currentPage - 1) * 10 + 1 }}-{{ currentPage * 10 }} of {{ totalHits }} entries </h4>
    <div v-for="searchResultEntry in getResultList"
        v-bind:key="searchResultEntry.url_link"
        v-bind:searchResultEntry="searchResultEntry">
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
    <b-pagination v-model="currentPage" :total-rows="rows" :per-page="perPage" aria-controls="my-table"></b-pagination>
  </div>
</template>

<script>
  export default {
    data() {
    return {
      isActive: false,
      perPage: 10,
      currentPage: 1,
      totalHits: this.searchResult.total_hits
      }
    },
    name:'SearchStyleResultList',
    props:['searchResult'],
    computed: {
      getResultList () {
      const items = this.searchResult.search_result
      // Return just page of items needed
      return items.slice(
        (this.currentPage - 1) * this.perPage,
        this.currentPage * this.perPage
      )
    },
      rows: function () {
        return this.searchResult.search_result.length
      }
    }
  }
</script>
