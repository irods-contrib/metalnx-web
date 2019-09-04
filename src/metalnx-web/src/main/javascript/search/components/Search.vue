<template>
  <div class="container">
    <div class="row">
      <b-input-group>
        <b-dropdown id="schemaSelection" slot="prepend" v-bind:text="searchLabel" variant="info">
                <b-dropdown-item  v-for="i in availableSearchSchema" :key="i" v-on:click="selectSchema(i)">{{i.schemaName}}</b-dropdown-item>

        </b-dropdown>
        <b-form-input v-model="searchText"></b-form-input>
       
          <b-button variant="success" slot="append" v-on:click="search()">Search</b-button>
        
      </b-input-group>
    </div>
    <div class="row">
       <div v-if="searchResult">
      <SearchStyleResult v-bind:searchResult="searchResult"></SearchStyleResult>
    </div>
    </div>
    
  </div>
</template>

<script>
import axios from 'axios'
import SearchStyleResult  from './SearchStyleResult'

export default {
  name: "Search",
  components: {
    SearchStyleResult
    
  },
  data() {
    return {
      searchLabel: 'Select Schema',
      selectedSearchSchema: '',
      availableSearchSchema:[],
      searchText: '',
      searchResult: ''
    }
  },
 
  created: function () {
       
        axios.get('/metalnx/api/search/indexes').then(response => (this.availableSearchSchema = response.data.searchSchemaEntry))
  }, 
  methods: {
      selectSchema: function(selected) {
        //console.log("eventData:" + eventData);
        this.selectedSearchSchema = selected;
        this.searchLabel = selected.schemaName;
      },
      search: function() {
          axios.post('/metalnx/api/search/textSearch', {
            endpointUrl: this.selectedSearchSchema.endpointUrl,
            indexId: this.selectedSearchSchema.schemaId,
            searchQuery: this.searchText,
            length: 0,
            offset: 0}
            ).then(response => this.searchResult = response.data)
      },
}
}

</script>
<style>
</style>
