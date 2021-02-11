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
          <b-dropdown
            id="schemaSelection"
            slot="prepend"
            v-bind:text="publishLabel"
            variant="light"
          >
            <b-dropdown-item
              v-for="i in availablePublishSchema"
              :key="i.schemaId"
              v-on:click="selectSchema(i)"
            >{{i.schemaName}}</b-dropdown-item>
          </b-dropdown>
          <b-button variant="primary" slot="append" v-on:click="exportSelected()">Export</b-button>
        </form>
      </nav>
      <b-table
        selectable
        striped
        head-variant="dark"
        hover
        :items="getGridData.items"
        :fields="getGridData.fields"
        responsive="sm"
      >
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
          />
        </template>
      </b-table>
    </div>
  </div>
</template>
{{ row.item.chkCol }}
<script>
import axios from "axios";
export default {
  name: "ShoppingCart",
  data() {
    return {
      cartList: [],
      selected: [],
      selectAll: false,
      publishLabel: "Select Schema",
      selectedPublishSchema: "",
      availablePublishSchema: [],
      publishResult: ""
    };
  },
  created: function() {
    axios({
      method: "get",
      url: "/metalnx/api/shoppingCart/getCart/"
    }).then(response => (this.cartList = response.data));

    axios({
      method: "get",
      url: "/metalnx/api/shoppingCart/info/"
    }).then(response => {
      this.availablePublishSchema = response.data.publishingSchemaEntry;
    });
  },
  computed: {
    getGridData: function() {
      var resultData = [];
      var pathDict = {};
      pathDict["path"] = this.cartList;
      for (var entry in this.cartList) {
        var temp = {};
        temp["path"] = this.cartList[entry];
        var pathSplit = this.cartList[entry].split("/");
        temp["name"] = pathSplit[pathSplit.length - 1];
        resultData.push(temp);
      }
      var data = {
        fields: [
          {
            key: "selected",
            label: ""
          },
          {
            key: "name",
            label: "Name",
            formatter: value => {
              return value;
            }
          },
          {
            key: "path",
            label: "Absolute Path",
            formatter: value => {
              return value;
            }
          }
        ],
        items: resultData
      };
      return data;
    }
  },
  methods: {
    selectSchema: function(selected) {
      this.selectedPublishSchema = selected;
      this.publishLabel = selected.schemaName;
    },
    select: function() {
      this.selected = [];
      this.publishResult = null;
      if (!this.selectAll) {
        for (let i in this.cartList) {
          this.selected.push(this.getGridData.items[i].path);
        }
      }
    },
    exportSelected: function() {
      var jsonData = {
        additionalProperties: {},
        id: ""
      };
      var publishRequestData = JSON.stringify(jsonData);

      axios({
        method: "post",
        url: "/metalnx/api/shoppingCart/publisher/",
        data: {
          endpointUrl: this.selectedPublishSchema.endpointUrl,
          indexId: this.selectedPublishSchema.schemaId,
          publishRequestData: publishRequestData
        }
      })
        .then(response => {
          this.publishResult = response.data;
          var responseType = "";
          if (this.publishResult) {
            if (
              this.publishResult.hasOwnProperty("response_type") ||
              this.publishResult.hasOwnProperty("response_path_or_link")
            ) {
              responseType = this.publishResult.response_type;
              switch (responseType) {
                case "download":
                  this.prepareFilesForDownload(
                    this.publishResult.response_path_or_link
                  );
                  break;
                case "redirect":
                  // add redirect code here
                  this.$bvToast.toast(
                    "Response type redirect not supported: ",
                    {
                      title: `Error publishing failed`,
                      variant: "danger",
                      solid: true
                    }
                  );
                  break;
                default:
                  this.$bvToast.toast("Response type not supported: ", {
                    title: `Error publishing failed`,
                    variant: "danger",
                    solid: true
                  });
              }
            } else {
              this.$bvToast.toast(
                "response_type or response_path_or_link property missing: ",
                {
                  title: `Error publishing failed`,
                  variant: "danger",
                  solid: true
                }
              );
            }
          } else {
            this.$bvToast.toast("Publish service returned empty result: ", {
              title: `Error publishing failed`,
              variant: "danger",
              solid: true
            });
          }
        })
        .catch(error => {
          // status code that is not in range of 2xx
          if (error.response) {
            this.$bvToast.toast(
              "Publisher returned error: " + error.response.status,
              {
                title: `Error publishing failed`,
                variant: "danger",
                solid: true
              }
            );
          } else if (error.request) {
            //request sent but no response was received `error.request`
            this.$bvToast.toast("Service not responding: " + error.request, {
              title: `Error publishing failed`,
              variant: "danger",
              solid: true
            });
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error publishing failed`,
                variant: "danger",
                solid: true
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error publishing failed`,
            variant: "danger",
            solid: true
          });
        });
    },
    prepareFilesForDownload: function(path) {
      var url =
        "/metalnx/fileOperation/prepareFilesForDownload/?paths" +
        encodeURIComponent("[]") +
        "=" +
        encodeURIComponent(path);
      axios({
        method: "get",
        url: url
      })
        .then(() => {
          this.handleDownload(path.split("/").pop());
        })
        .catch(error => {
          if (error.response) {
            // status code that is not in range of 2xx
            this.$bvToast.toast(
              "service failed to prepare file download: " +
                error.response.status,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true
              }
            );
          } else if (error.request) {
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true
              }
            );
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error preparing file download`,
            variant: "danger",
            solid: true
          });
        });
    },

    handleDownload: function(filename) {
      console.log("handleDownload()");
      axios({
        url: "/metalnx/fileOperation/download/",
        method: "GET",
        responseType: "blob"
      })
        .then(response => {
          console.log("successful: download call");
          console.log(response.data);
          this.$bvToast.toast("Downloading export file", {
            title: `File download`,
            variant: "success",
            solid: true
          });
          var fileURL = window.URL.createObjectURL(new Blob([response.data]));
          var fileLink = document.createElement("a");

          fileLink.href = fileURL;
          fileLink.setAttribute("download", filename);
          document.body.appendChild(fileLink);

          fileLink.click();
        })
        .catch(error => {
          if (error.response) {
            // status code that is not in range of 2xx
            this.$bvToast.toast(
              "service failed to while file download: " + error.response.status,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true
              }
            );
          } else if (error.request) {
            //request sent but no response was received `error.request`
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true
              }
            );
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error file download`,
            variant: "danger",
            solid: true
          });
        });
    }
  }
};
</script>
<style>
</style>