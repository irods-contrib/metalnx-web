<template>
  <!-- Search -->
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

    <!-- Search hint -->
    <div class="mt-2">
      <b-link @click=toggle() variant="info">
      {{ showSearchHint ? 'Hide Hint' : 'Show Search Hint' }}</b-link>
      <b-alert
        variant="info"
        dismissible
        fade
        :show="showSearchHint"
        @dismissed="showSearchHint=false">
        <div>
          <h5>Valid options:</h5>
          <ol>
            <li>Text Search <b>Example =></b> FSHD2</li>
            <li>Include search fields for advanced search. <b>Example =></b> ProjectID: RNASeq* Hypothesis: sci-RNAseq</li>
          </ol>
          <p> Wrap text search with double-quotes for exact search. Default operator is OR, use AND explicitly if needed. </p>
        </div>
        <div v-if="availableSearchAttributes.length">
          <b-table striped small hover :items="availableSearchAttributes" :fields="fields" tdClass="searchFieldTable"></b-table>
          </div>
      </b-alert>
    </div>

    <!-- Search Results -->
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
      fields: [
          {
            key: 'attribName',
            label: 'Available Fields',
            sortable: true
          },
          {
            key: 'info',
            label: 'Description',
            sortable: false,
            tdClass: 'searchFieldsDescription'
          },
          {
            key: 'attribExample',
            label: 'Example'
          }
        ],
      showSearchHint: false,
      searchLabel: 'Select Schema',
      selectedSearchSchema: '',
      availableSearchSchema:[],
      availableSearchAttributes: [],
      searchText: '',
      searchResult: '',
      patt: /[a-zA-Z0-9]*:/g,
      matchAll: [],
      searchTerms: [],
      validation: true
    }
  },
 
  created: function () {  
    axios.get('/metalnx/api/search/indexes').then(response => (this.availableSearchSchema = response.data.searchSchemaEntry))
  },

  methods: {
    selectSchema: function(selected) {
      this.selectedSearchSchema = selected;
      this.searchLabel = selected.schemaName;
      axios.get('/metalnx/api/search/attributes/', {
        params: {
          index_name: this.selectedSearchSchema.schemaId,
          endpointUrl: this.selectedSearchSchema.endpointUrl
        }
      }).then(response => (this.availableSearchAttributes = response.data.attributes))
    },
    toggle: function(){
            this.showSearchHint = !this.showSearchHint
        },

    search: function() {
      if (this.selectedSearchSchema.schemaId == null) {
        this.$bvToast.toast('Schema not selected!', {
          title: `Error`,
          variant: 'danger',
          solid: true
        })
      } else if (this.searchText == "") {
        this.$bvToast.toast('Search text cannot be empty!', {
          title: `Error`,
          variant: 'danger',
          solid: true
        })
      } else {
        this.matchAll = this.searchText.match(this.patt)
        if (this.matchAll != null) {
          var i;
          var j;
          var temp;
          for (j = 0; j < this.availableSearchAttributes.length; j++) {
            this.searchTerms.push(this.availableSearchAttributes[j].attribName)
          }
          for (i = 0; i < this.matchAll.length; i++){
            temp = this.matchAll[i]
            temp = temp.substring(0, temp.length-1)
            if (!this.searchTerms.includes(temp)){
              this.validation = false
              break
            }
          }
        }
        if (this.validation){
          this.searchResult = null
          axios.post('/metalnx/api/search/textSearch', {
          endpointUrl: this.selectedSearchSchema.endpointUrl,
          indexId: this.selectedSearchSchema.schemaId,
          searchQuery: this.searchText,
          length: 0,
          offset: 0}
          ).then(response => this.searchResult = response.data)
          if(this.searchResult === 0 || this.searchResult === '') this.searchResult = null;
          if (this.showSearchHint){
            this.showSearchHint = false
          }
        } else {
          this.searchResult = null
          this.$bvToast.toast('Invalid search term in search text!', {
          title: `Error`,
          variant: 'danger',
          solid: true
        })
        this.validation = true
        }
      }  
    },
  }
}
</script> 
<style>
</style>
