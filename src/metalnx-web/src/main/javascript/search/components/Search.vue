<template>
  <div class="container">
    <div class="row">
      <b-input-group>
      <b-dropdown id="schemaSelection" slot="prepend" v-bind:text="searchLabel" variant="info">
              <b-dropdown-item  v-for="i in availableSearchSchema" :key="i" v-on:click="selectSchema(i)">{{i.schemaName}}</b-dropdown-item>

      </b-dropdown>
    <b-form-input></b-form-input>
      </b-input-group>
    </div>
       Hello!
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
  }
};

</script>
<style>
</style>
