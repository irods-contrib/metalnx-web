<template>
  <div>
    <h1>Search Results</h1>
    <div>
     <b-button-toolbar key-nav aria-label="Toolbar with button groups">
        <b-button-group class="mx-1">
          <b-button v-on:click="layout = 'list'" v-bind:class="{ 'active': layout == 'list'}" title="List">List</b-button>
          <b-button v-on:click="layout = 'grid'" v-bind:class="{ 'active': layout == 'grid'}" title="Grid">Grid</b-button>
        </b-button-group>
     </b-button-toolbar>
    </div>
	<div class="tool-bar">
		<!-- These link buttons use Vue.js to bind click events to change the "layout" variable and bind an active class -->
		<a class="" v-on:click="layout = 'list'" v-bind:class="{ 'active': layout == 'list'}" title="List"></a>
		<a class="" v-on:click="layout = 'grid'" v-bind:class="{ 'active': layout == 'grid'}" title="Grid"></a>
	</div>

	<!-- Vue.js lets us choose which UL to show depending on the "layout" variable -->
	
	<div v-if="layout === 'grid'" class="grid">
    <div v-if="searchResult.search_result.length > 0">
      <SearchStyleResultGrid 
        v-bind:searchResult="searchResult">
      </SearchStyleResultGrid>
    </div>
	</div>

	<div v-if="layout === 'list'" class="list">
		<div v-if="searchResult.search_result.length > 0">
      <SearchStyleResultList
        v-for="searchResultEntry in searchResult.search_result"
        v-bind:key="searchResultEntry.url_link"
        v-bind:searchResultEntry="searchResultEntry">
      </SearchStyleResultList>
    </div>
    <div v-else>
      No match found
    </div>
	</div>

    
  </div>
</template>

<script>
import SearchStyleResultList  from './SearchStyleResultList'
import SearchStyleResultGrid from './SearchStyleResultGrid'
  export default {
    name:'SearchStyleResult',
    components: {
            SearchStyleResultList,
            SearchStyleResultGrid
    },
    data() {
    return {
      layout: 'grid'
    }
  },
    props:['searchResult']
  }
</script>
