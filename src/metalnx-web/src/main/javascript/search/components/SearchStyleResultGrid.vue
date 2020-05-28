<template>
<div>
<b-table striped head-variant="dark" hover :items="getGridData.items" :fields="getGridData.fields">
  <template v-slot:cell(customTitle)="data">
    <div v-if="data.item.propertyDict.isFile" class="listFile">
      <b-link v-bind:href="data.item.url_link" target="_blank">{{ data.item.title}}</b-link>
    </div>
    <div v-else>
      <b-link v-bind:href="data.item.url_link" target="_blank"><Fa :icon="faFolder"/>  {{ data.item.title}}</b-link>
    </div>
  </template>
  <template v-slot:cell(propertyDict.dataSize)="data">
    <div v-if="data.item.propertyDict.isFile" class="listFile">
      {{data.item.propertyDict.dataSize}}
    </div>
    <div v-else>
      -
    </div>
  </template>
</b-table>
</div>
</template>

<script>
  import Fa from 'vue-fa'
  import { faFolder } from '@fortawesome/free-solid-svg-icons'

  export default {
    components: {
    Fa
    },
    data() {
    return {
      isActive: false,
      faFolder
    }
    },
    name:'SearchStyleResultGrid',
    props:['searchResult'],
    computed: {
      getGridData:  function () {
        var resultData = [];
        for (var entry in this.searchResult.search_result) {
          // Converting properties array to dictionary
          var propertySet = this.searchResult.search_result[entry].properties.propertySet
          var propertyDict = {};
          for (var propertyEntry in propertySet){
            propertyDict[propertySet[propertyEntry].name] = propertySet[propertyEntry].value
          }
          this.searchResult.search_result[entry]['propertyDict'] = propertyDict
          resultData.push(this.searchResult.search_result[entry])
        }
        
        var data = {
        fields: [
          {
            key: 'customTitle',
            label: 'Name',
            formatter: value => {
              return value
            }
          },
          {
            key: 'propertyDict.lastModifiedDate',
            label: 'Modified',
            formatter: value => {
              return new Date(value * 1000)
            }
          },
          {
            key: 'propertyDict.dataSize',
            label: 'Size (kB)',
            formatter: value => {
              return value
            }
          }
        ],
        items: resultData
        }
        return data;
      }
    }
  }
</script>
<style scoped>

</style>