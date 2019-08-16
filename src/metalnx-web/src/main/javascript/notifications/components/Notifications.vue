<template>
  <div>
    <div class="form-simple-search">
        <button v-on:click="getData()">Notifications</button>
    </div>
    <NTable v-bind:results="results"></NTable>
   
  </div>
</template>

<script>
import NTable from './NTable'
import axios from 'axios'

export default {
  name: 'Notifications',
    data () {
    return {
      results: []
    }
  },
  components: {
    NTable
  },
   methods: {
    getData () {
        const url = 'http://localhost:8800/v2/notification/messages/test1'
        axios.get(url).then(response => (this.results = response.data))
    }
  }
}
</script>
