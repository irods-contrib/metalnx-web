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
    
  selected:{{selectedSearchSchema}}
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: "Search",
  data() {
    return {
      searchLabel: 'Select Schema',
      selectedSearchSchema: '',
      availableSearchSchema:[],
      searchText: '',
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
          axios.post('/metalnx/api/search', {
            endpointUrl: this.selectedSearchSchema.endpointUrl,
            index_name: this.selectedSearchSchema.schemaId,
            search_query: this.searchText}
            ).then(response => (console.log("response:" + response)))
      },
}
}

</script>
<style>
</style>
