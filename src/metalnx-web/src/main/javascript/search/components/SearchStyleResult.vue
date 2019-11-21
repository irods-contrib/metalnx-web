<template>
  <div>
    <h1>Search Results</h1>
    <div>
      <SearchStyleResultEntry
        v-for="searchResultEntry in searchResult.search_result"
        v-bind:key="searchResultEntry.url_link"
        v-bind:searchResultEntry="searchResultEntry">
      </SearchStyleResultEntry>
    </div>
  </div>
</template>

<script>
import SearchStyleResultEntry  from './SearchStyleResultEntry'

  export default {
    name:'SearchStyleResult',
    components: {
            SearchStyleResultEntry
    },
    props:['searchResult']
  }
</script>
