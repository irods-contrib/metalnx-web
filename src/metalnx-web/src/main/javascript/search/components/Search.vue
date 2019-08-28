<template>
  <div class="container">
    <div class="row">
      <b-input-group>
      <b-dropdown id="schemaSelection" slot="prepend" text="Dropdown" variant="info">
              <b-dropdown-item  v-for="i in availableSearchSchema" :key="i" >{{i.schemaName}}</b-dropdown-item>

      </b-dropdown>
    <b-form-input></b-form-input>
      </b-input-group>
    </div>
       

  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: "Search",
  data() {
    return {
    selectedSearchSchema: '',
    availableSearchSchema:[]
    }
  },

  created: function () {
        axios.get('/metalnx/api/search/indexes').then(response => (this.availableSearchSchema = response.data.searchSchemaEntry))
  }
};

</script>
<style>
</style>
