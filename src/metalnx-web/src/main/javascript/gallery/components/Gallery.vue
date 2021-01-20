<template>
  <!-- Search -->
  <div>
    <div class="widgetsContainer">
      <span>{{ formattedPath }}</span>
      <span class="widgets">
        <b-link
          href="#"
          class="btn btn-default icon-btn"
          id="showCollectionFormBtn"
          aria-label="add collection"
          title="add collection"
        >
          <i class="fa fa-folder"></i>
        </b-link>
        <b-link
          class="btn btn-default icon-btn"
          :href="`/metalnx/collectionInfo${url}`"
          id="infoIcon"
          title="View info"
          ><i class="fa fa-info-circle fa-color"></i>
        </b-link>
        <b-link
          href="#"
          class="btn btn-default icon-btn"
          aria-label="upload files"
          id="uploadIcon"
          title="Upload file(s) to iRods"
        >
          <i class="fa fa-cloud-upload fa-color"></i>
        </b-link>
        <b-link
          :href="`/metalnx/collections${url}`"
          class="btn btn-default icon-btn"
          id="collectionIcon"
          title="Collection List View"
          ><i class="fa fa-list fa-lg" aria-hidden="true"></i
        ></b-link>
      </span>
    </div>
    <div class="paginationContainer">
    <b-pagination-nav 
      active="true"
      class="gallery-nav"
      v-model="currentPage"
      :link-gen="linkGen"
      :number-of-pages="10"
    />
    <b-form-select
          v-model="size_selected"
          class="thumbnail-size-select"
          :options="size_options"
        >
        <b-form-select-option :value="null" disabled>-- Select thumbnail size --</b-form-select-option>
    </b-form-select>
    <b-form-select
          v-model="items_selected"
          class="thumbnail-items-select"
          :options="items_options"
          @change="updateItemPerPage(items_selected)"
        >
        <b-form-select-option :value="null" disabled>-- Select items per page --</b-form-select-option>
    </b-form-select>
    </div>
    <div class="galleryContainer">
      <div class="gallery" v-for="item in demo.items">
        <a
          :id="'thumbnail' + item.id"
          :href="`/metalnx/collectionInfo${url}%2F${item.name}`"
        >
          <img v-bind:class="[size_selected]" :src="item.thumbnails" />
          <div class="thumbnail_name">{{ item.name }}</div>
        </a>
        <b-tooltip :target="'thumbnail' + item.id" triggers="hover">
          <div>File Size: {{ item.fileSize }}</div>
          <div>File Type: {{ item.fileType }}</div>
        </b-tooltip>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Gallery",
  components: {},
  data() {
    const currentUrl = window.location;
    const url = window.location.search;
    const params = new URLSearchParams(url.substring(1));
    const path = decodeURIComponent(params.get("path"));
    const formattedPath = path.substring(1).replaceAll("/", " >> ");

    // Retrieve items per page and current page from url
    const perPage = params.get("perPage");
    const currentPage = params.get("currentPage");

    let response = axios({
      method: "GET",
      url: "http://localhost/metalnx/api/gallery",
      params: {
        path: "/tempZone/home/test1",
        offset: 0,
        limit: 100,
      },
    }).catch((e) => console.log(e));

    var demo = {
      location: "testLocation",
      items: [
        {
          id: 1,
          name: "sample1.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample1.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 2,
          name: "sample2.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample2.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 3,
          name: "sample3.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample3.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 4,
          name: "sample4.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample7.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 5,
          name: "sample5.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample5.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 6,
          name: "sample6.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample6.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 7,
          name: "sample7.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample7.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 8,
          name: "sample8.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample8.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 9,
          name: "sample9.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample9.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
        {
          id: 10,
          name: "sample10.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample10.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics",
        },
      ],
    };
    return {
      demo,
      currentUrl,
      url,
      path,
      formattedPath,
      currentPage,
      perPage,
      response,
      size_selected: null,
      size_options: ["small", "medium", "large"],
      items_selected: null,
      items_options: [25,50,75,100],
    };
  },
  methods: {
    linkGen(pageNum) {
      return `/gallery?path=${encodeURIComponent(this.path)}&currentPage=${pageNum}&perPage=${this.perPage}`
    },
    updateItemPerPage(newPerPage){
      window.location = `/gallery?path=${encodeURIComponent(this.path)}&currentPage=${this.currentPage}&perPage=${newPerPage}`;
    }
  }
};
</script> 
<style>
.large {
  width: 125px;
  height: 125px;
}

.galleryContainer {
  max-width: 100%;
  margin-left: 20px;
  margin-top: 40px;
  margin-right: 20px;
  padding-left: 15px;
  padding-right: 15px;
  display: flex;
  flex-flow: row wrap;
  align-content: flex-start;
}

.medium {
  width: 100px;
  height: 100px;
}

.small {
  width: 75px;
  height: 75px;
}

.thumbnail_select {
  width: 100px;
  margin: 20px 10px auto 40px;
}

.gallery {
  margin: 10px 20px 10px 20px;
  text-align: center;
}

.gallery-nav{
  margin: 10px 10px 10px 30px;
}

.thumbnail_name {
  margin: 5px auto 5px auto;
}

.collection_view {
  float: right;
  margin-top: 25px;
  margin-right: 140px;
}

.paginationContainer{
  display: flex;
  flex-direction: row;
}

.widgetsContainer {
  margin-top: 20px;
  margin-left: 20px;
  display: "flex";
  flex-direction: column;
}
.thumbnail-size-select{
  margin-top: 10px;
  margin-left: 10px;
  width: auto;
}

.thumbnail-items-select{
  margin-top: 10px;
  margin-left: 10px;
  width: auto;
}

.widgets {
  margin-left: 35%;
}
</style>
