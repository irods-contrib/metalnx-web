<template>
  <div>
    <h1>Search Results</h1>
    <div v-if="searchResult.search_result.length > 0">
      <SearchStyleResultEntry
        v-for="searchResultEntry in searchResult.search_result"
        v-bind:key="searchResultEntry.url_link"
        v-bind:searchResultEntry="searchResultEntry">
      </SearchStyleResultEntry>
    </div>
    <div v-else>
      No match found
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
