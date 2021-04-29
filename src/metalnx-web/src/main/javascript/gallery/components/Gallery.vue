<template>
  <!-- Search -->
  <div>
    <div class="gallery-widgets-container">
      <GalleryPath v-bind:pathArray="pathArray" />
      <GalleryWidgets v-bind:url="url" />
    </div>
    <!-- <GalleryPagination v-bind:url="url" /> -->
    <div v-if="!thumbnails || thumbnails['error']">
      <b-alert show variant="danger" class="gallery-notification"
        >{{ thumbnail_error_message }} Please click
        <b-link :href="`/metalnx/collections${url}`">here</b-link> to go back to
        collection browser.</b-alert
      >
    </div>
    <div v-else class="gallery-item-container">
      <GalleryItem
        v-for="item in thumbnails.items"
        v-bind:path="fullPath"
        v-bind:item="item"
        :key="item.id"
      />
    </div>
  </div>
</template>

<script>
import axios from "axios";
import GalleryItem from "./GalleryItem.vue";
import GalleryPath from "./GalleryPath.vue";
import GalleryWidgets from "./GalleryWidgets.vue";
import GalleryPagination from "./GalleryPagination.vue";

export default {
  name: "Gallery",
  components: { GalleryItem, GalleryPath, GalleryWidgets, GalleryPagination },
  data() {
    const url = window.location.search;
    // Retrieve items per page and current page from url
    const params = new URLSearchParams(url.substring(1));
    const fullPath = decodeURIComponent(params.get("path"));
    const formattedPath = fullPath.substring(1);
    const pathArray = formattedPath.split("/");
    const currPerPage =
      params.get("perPage") === null ? 10 : params.get("perPage");
    const currPage = params.get("page") === null ? 1 : params.get("page");
    const currSize = params.get("size") === null ? "small" : params.get("size");
    return {
      url,
      fullPath,
      pathArray,
      formattedPath,
      currPerPage,
      currPage,
      currSize,
      thumbnails: {},
      thumbnail_error_message:
        "An error has occurred while fetching thumbnail data from iRODS.",
    };
  },
  methods: {
    async fetchThumbnails() {
      try {
        let response = await axios({
          method: "GET",
          url: "/metalnx/api/gallery",
          params: {
            path: this.fullPath,
            offset: this.currPerPage * (this.currPage - 1),
            limit: this.currPerPage,
          },
        });
        this.thumbnails = response.data;
      } catch (err) {
        if (err.response) {
          this.thumbnail_error_message += err.response;
        } else if (err.request) {
          this.thumbnail_error_message += " Client never receives response.";
        } else {
          this.thumbnail_error_message += " Client error.";
        }
      }
    },
  },
  mounted() {
    this.fetchThumbnails();
  },
};
</script> 
<style>
.gallery-widgets-container {
  max-width: 100%;
  display: flex;
  flex-flow: row wrap;
  align-content: space-between;
}

.gallery-notification {
  text-align: center;
}

.gallery-item-container {
  display: flex;
  flex-flow: row wrap;
  align-content: flex-start;
}
</style>
