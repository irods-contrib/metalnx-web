<template>
  <div class="container">
    <div class="row">
      <b-input-group>
        <b-dropdown id="schemaSelection" slot="prepend" v-bind:text="searchLabel" variant="info">
          <b-dropdown-item  v-for="i in availableSearchSchema" :key="i.schemaId" v-on:click="selectSchema(i)">{{i.schemaName}}</b-dropdown-item>
        </b-dropdown>
        <b-form-input 
            id="search"
            size="lg"
            class="form-control mr-2"
            type="text"
            required
            autofocus
            placeholder="Enter a search"
            v-model="searchText"></b-form-input>
        <b-button variant="success" slot="append" v-on:click="search()">Search</b-button>
      </b-input-group>
    </div>
    
    <div v-if="availableSearchAttributes.length">
      <table id="firstTable">
        <thead>
          <tr>
            <th>Attribute Name</th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in availableSearchAttributes" :key="item.shortcutText">
            <td>{{item.attribName}}</td>
            <td>{{item.info}}</td>
          </tr>
        </tbody>
      </table>
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
      availableSearchAttributes: [],
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
      axios.get('/metalnx/api/search/attributes/', {
        params: {
          index_name: this.selectedSearchSchema.schemaId,
          endpointUrl: this.selectedSearchSchema.endpointUrl
        }
      }).then(response => (this.availableSearchAttributes = response.data.attributes))
    },
    search: function() {
      
      axios.post('/metalnx/api/search/textSearch', {
      endpointUrl: this.selectedSearchSchema.endpointUrl,
      indexId: this.selectedSearchSchema.schemaId,
      searchQuery: this.searchText,
      length: 0,
      offset: 0}
      ).then(response => this.searchResult = response.data)
      this.availableSearchAttributes = []
    },
  }
}

</script>
<style>
</style>
