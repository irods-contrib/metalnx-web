<template>
<div class="container">
    <h1>Cart List</h1>
    <div v-if="cartList.length > 0">
      <nav class="navbar navbar-dark bg-secondary">
       <div>
          <b-dropdown text="Actions" variant="light">
            <b-dropdown-item href="#">Clear Cart</b-dropdown-item>
            <b-dropdown-item href="#">Remove item(s)</b-dropdown-item>
          </b-dropdown>
        </div>
        <form class="form-inline">
          <b-form-select
            id="inline-form-custom-select-pref"
            class="mb-2 mr-sm-2 mb-sm-0"
            :options="[{ text: 'Choose...', value: null }, 'Bagit', 'BDBag', 'GEO', '.tar', '.zip']"
            :value="null"
          ></b-form-select>
          <button class="btn btn-primary my-2 my-sm-0" type="submit" @click="exportSelected">Export</button>
        </form>
      </nav>
      <b-table selectable striped head-variant="dark" hover :items="getGridData.items" :fields="getGridData.fields" responsive="sm">
        <!-- A custom formatted header cell for field 'name' -->
        <template v-slot:head(selected)="data">
          <input type="checkbox" v-model="selectAll" @click="select" />
        </template>
        <template v-slot:cell(selected)="row">
          <b-form-group>
              <input type="checkbox" :value="row.item.path" v-model="selected" />
          </b-form-group>
        </template>
        <template v-slot:table-colgroup="scope">
    <col
      v-for="field in scope.fields"
      :key="field.key"
      :style="{ width: field.key === 'selected' ? '20px' : '180px' }"
    >
        </template>
      </b-table>
    </div>
</div>
</template>
{{ row.item.chkCol }}
<script>
import axios from 'axios'
export default {
  name: 'ShoppingCart',
  data(){
    return {
      cartList: [],
      selected: [],
      selectAll: false
    }
  },
  created: function () {  
    axios.get('/metalnx/api/shoppingCart/getCart/').then(response => (this.cartList = response.data))
  },
  computed: {
    getGridData:  function () {
      var resultData = [];
      var pathDict = {};
      pathDict['path'] = this.cartList
      for (var entry in this.cartList) {
        var temp = {}
        temp["path"] = this.cartList[entry]
        var pathSplit = this.cartList[entry].split("/")
        temp["name"] = pathSplit[pathSplit.length - 1]
        resultData.push(temp)
      }
      var data = {
        fields: [
          {
            key: "selected",
            label: "", 
          },
          {
            key: 'name',
            label: 'Name',
            formatter: value => {
              return value
            }
          },
          {
            key: 'path',
            label: 'Absolute Path',
            formatter: value => {
              return value
            },
          }
        ],
        items: resultData
      }
      return data;
    }
  },
  methods: {
    select: function() {
			this.selected = [];
			if (!this.selectAll) {
				for (let i in this.cartList) {
					this.selected.push(this.getGridData.items[i].path);
				}
			}
    },
    exportSelected: function() {
      alert('exportSelected clicked');
    }
  }
}
</script>
<style>
</style>