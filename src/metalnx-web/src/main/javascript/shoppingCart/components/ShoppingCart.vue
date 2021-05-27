<template>
  <div class="container">
    <b-overlay :show="show" rounded="sm">
      <h1>Cart List</h1>
      <nav class="navbar navbar-dark bg-secondary">
        <div>
          <b-dropdown
            id="cartActions"
            slot="prepend"
            text="Actions"
            variant="info"
          >
            <b-dropdown-item v-on:click="clearCartItems()"
              >Clear cart</b-dropdown-item
            >
            <b-dropdown-item v-on:click="removeSelectedCartItems()"
              >Remove items(s)</b-dropdown-item
            >
          </b-dropdown>
        </div>
        <form class="form-inline">
          <b-dropdown
            id="schemaSelection"
            slot="prepend"
            v-bind:text="publishSchemaName"
            variant="light"
          >
            <b-dropdown-item
              v-for="i in availablePublishSchema"
              :key="i.schemaId"
              v-on:click="selectSchema(i)"
              >{{ i.schemaName }}</b-dropdown-item
            >
          </b-dropdown>
          <b-button
            variant="primary"
            slot="append"
            v-on:click="exportSelected()"
            >Export</b-button
          >
        </form>
      </nav>
      <div v-if="getGridData.items.length > 0">
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
          <template v-slot:head(selectedCartItems)="data">
            <input
              type="checkbox"
              v-model="isSelectAllCartItems"
              @click="updateCartItemList"
            />
          </template>
          <template v-slot:cell(selectedCartItems)="row">
            <b-form-group>
              <input
                type="checkbox"
                :value="row.item.path"
                v-model="selectedCartItems"
              />
            </b-form-group>
          </template>
          <!-- A custom formatted column -->
          <template #cell(path)="path">
            <b-link v-bind:href="'/metalnx/collectionInfo?path=' + path.item.path">{{ path.item.path }}</b-link>
          </template>
          <template v-slot:table-colgroup="scope">
            <col
              v-for="field in scope.fields"
              :key="field.key"
              :style="{
                width: field.key === 'selectedCartItems' ? '20px' : '180px',
              }"
            />
          </template>
        </b-table>
      </div>
      <div v-else>Shopping cart is empty</div>
    </b-overlay>
  </div>
</template>
{{ row.item.chkCol }}
<script>
import axios from "axios";
export default {
  name: "ShoppingCart",
  data() {
    return {
      cartItems: [],
      selectedCartItems: [],
      isSelectAllCartItems: false,
      publishSchemaName: "Select Schema",
      selectedPublishSchema: "",
      availablePublishSchema: [],
      publishResult: "",
      show: false,
    };
  },
  created: function () {
    axios({
      method: "get",
      url: "/metalnx/api/shoppingCart/getCart/",
    }).then((response) => (this.cartItems = response.data));

    axios({
      method: "get",
      url: "/metalnx/api/shoppingCart/info/",
    }).then((response) => {
      this.availablePublishSchema = response.data.publishingSchemaEntry;
    });
  },
  computed: {
    getGridData: function () {
      var resultData = [];
      var pathDict = {};
      pathDict["path"] = this.cartItems;
      for (var entry in this.cartItems) {
        var temp = {};
        temp["path"] = this.cartItems[entry];
        var pathSplit = this.cartItems[entry].split("/");
        temp["name"] = pathSplit[pathSplit.length - 1];
        resultData.push(temp);
      }
      var data = {
        fields: [
          {
            key: "selectedCartItems",
            label: "",
          },
          {
            key: "name",
            label: "Name",
            formatter: (value) => {
              return value;
            },
          },
          {
            key: "path",
            label: "Absolute Path",
            formatter: (value) => {
              return value;
            },
          },
        ],
        items: resultData,
      };
      return data;
    },
  },
  methods: {
    selectSchema: function (selectedCartItems) {
      this.selectedPublishSchema = selectedCartItems;
      this.publishSchemaName = selectedCartItems.schemaName;
    },
    clearCartItems: function () {
      this.cartItems = [];
      axios.post("/metalnx/api/shoppingCart/clearCart");
    },
    removeSelectedCartItems: function () {
      axios({
        method: "post",
        url: "/metalnx/api/shoppingCart/removeFromCart/",
        data: {
          paths: this.selectedCartItems,
        },
      }).then((response) => {
        this.cartItems = response.data;
        this.selectedCartItems = [];
        //this.updateCartItemList();
      });
    },
    updateCartItemList: function () {
      this.selectedCartItems = [];
      this.publishResult = null;
      if (!this.isSelectAllCartItems) {
        for (let i in this.cartItems) {
          this.selectedCartItems.push(this.getGridData.items[i].path);
        }
      }
    },
    exportSelected: function () {
      this.show = true;
      var jsonData = {
        additionalProperties: {},
        id: "",
      };
      var publishRequestData = JSON.stringify(jsonData);
      axios({
        method: "post",
        url: "/metalnx/api/shoppingCart/publisher/",
        data: {
          endpointUrl: this.selectedPublishSchema.endpointUrl,
          indexId: this.selectedPublishSchema.schemaId,
          publishRequestData: publishRequestData,
        },
      })
        .then((response) => {
          this.publishResult = response.data;
          var responseType = "";
          if (this.publishResult) {
            if (
              this.publishResult.hasOwnProperty("response_type") ||
              this.publishResult.hasOwnProperty("response_path_or_link")
            ) {
              responseType = this.publishResult.response_type;
              switch (responseType) {
                case "error":
                  this.show = false;
                  this.$bvToast.toast(this.publishResult.response_message, {
                    title: `Error publishing failed`,
                    variant: "danger",
                    solid: true,
                    noAutoHide: true,
                  });
                  break;
                case "download":
                  this.prepareFilesForDownload(
                    this.publishResult.response_path_or_link
                  );
                  break;
                case "redirect":
                  // add redirect code here
                  this.show = false;
                  this.$bvToast.toast(
                    "Response type redirect not supported: ",
                    {
                      title: `Error publishing failed`,
                      variant: "danger",
                      solid: true,
                      noAutoHide: true,
                    }
                  );
                  break;
                default:
                  this.show = false;
                  this.$bvToast.toast("Response type not supported: ", {
                    title: `Error publishing failed`,
                    variant: "danger",
                    solid: true,
                    noAutoHide: true,
                  });
              }
            } else {
              this.show = false;
              this.$bvToast.toast(
                "response_type or response_path_or_link property missing: ",
                {
                  title: `Error publishing failed`,
                  variant: "danger",
                  solid: true,
                  noAutoHide: true,
                }
              );
            }
          } else {
            this.show = false;
            this.$bvToast.toast("Publish service returned empty result: ", {
              title: `Error publishing failed`,
              variant: "danger",
              solid: true,
              noAutoHide: true,
            });
          }
        })
        .catch((error) => {
          // status code that is not in range of 2xx
          this.show = false;
          if (error.response) {
            this.$bvToast.toast(
              "Publisher returned error: " + error.response.status,
              {
                title: `Error publishing failed`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          } else if (error.request) {
            //request sent but no response was received `error.request`
            this.$bvToast.toast("Service not responding: " + error.request, {
              title: `Error publishing failed`,
              variant: "danger",
              solid: true,
              noAutoHide: true,
            });
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error publishing failed`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error publishing failed`,
            variant: "danger",
            solid: true,
            noAutoHide: true,
          });
        });
    },
    prepareFilesForDownload: function (path) {
      var url =
        "/metalnx/fileOperation/prepareFilesForDownload/?paths" +
        encodeURIComponent("[]") +
        "=" +
        encodeURIComponent(path);
      axios({
        method: "get",
        url: url,
      })
        .then(() => {
          this.handleDownload(path.split("/").pop());
        })
        .catch((error) => {
          this.show = false;
          if (error.response) {
            // status code that is not in range of 2xx
            this.$bvToast.toast(
              "service failed to prepare file download: " +
                error.response.status,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          } else if (error.request) {
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error preparing file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error preparing file download`,
            variant: "danger",
            solid: true,
            noAutoHide: true,
          });
        });
    },

    handleDownload: function (filename) {
      console.log("handleDownload()");
      axios({
        url: "/metalnx/fileOperation/download/",
        method: "GET",
        responseType: "blob",
      })
        .then((response) => {
          console.log("successful: download call");
          console.log(response.data);
          this.$bvToast.toast(this.publishResult.response_message, {
            title: `File download`,
            variant: "success",
            solid: true,
            noAutoHide: true,
          });
          this.show = false;
          var fileURL = window.URL.createObjectURL(new Blob([response.data]));
          var fileLink = document.createElement("a");

          fileLink.href = fileURL;
          fileLink.setAttribute("download", filename);
          document.body.appendChild(fileLink);

          fileLink.click();
        })
        .catch((error) => {
          this.show = false;
          if (error.response) {
            // status code that is not in range of 2xx
            this.$bvToast.toast(
              "service failed to while file download: " + error.response.status,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          } else if (error.request) {
            //request sent but no response was received `error.request`
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          } else {
            // error in setting up the request
            this.$bvToast.toast(
              "failed in setting up the request: " + error.message,
              {
                title: `Error file download`,
                variant: "danger",
                solid: true,
                noAutoHide: true,
              }
            );
          }
          this.$bvToast.toast("Configuration error: " + error.config, {
            title: `Error file download`,
            variant: "danger",
            solid: true,
            noAutoHide: true,
          });
        });
    },
  },
};
</script>
<style>
</style>