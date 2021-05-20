<template>
  <!-- Search -->
  <div>
    <div class="gallery-widgets-container">
      <GalleryPath v-bind:pathArray="pathArray" />
      <GalleryWidgets v-bind:url="url" />
    </div>
    <!-- <GalleryPagination v-bind:url="url" /> -->
    <div class="gallery-loading-spinner">
      <b-spinner
        v-if="isLoading"
        variant="primary"
        label="Spinning"
      ></b-spinner>
    </div>
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
      isLoading: false,
    };
  },
  methods: {
    async fetchThumbnails() {
      try {
        this.isLoading = true;
        let response = await axios({
          method: "GET",
          url: "/metalnx/api/gallery",
          params: {
            path: this.fullPath,
            offset: this.currPerPage * (this.currPage - 1),
            limit: this.currPerPage,
          },
        });
        let gallery = response.data;

      // Note: This will iterate through each gallery item and load its thunbmail based on the path rule engine provided
        for (let i = 0; i < gallery.items.length; i++) {
          try {
            if (gallery.items[i].collection) continue;
            // call the previewPreparation in collection browser to fetch the raw image data
            let preview_prep_response = await axios({
              method: "GET",
              url: "/metalnx/previewPreparation/",
              params: {
                path: `${gallery.items[i].thumbnail}`,
              },
            });
            // parse the response from previewPreparation to an html element
            let parser = new DOMParser();
            let doc = parser.parseFromString(
              preview_prep_response.data,
              "text/html"
            );
            // check whether the thumbnail path provided is valid, if invalid, a broken image will be displayed
            if (
              doc.children[0].children[1].children[0].children[0].children[0]
                .tagName === "IMG"
            ) {
              gallery.items[i]["previewSrc"] = './';
            } else {
              // if the path is valid, call dataObjectPreview endpoint to fetch blog image
              let preview_response = await axios({
                method: "GET",
                url: "/metalnx/preview/dataObjectPreview/",
                responseType: "blob",
              });
              let previewSrc = URL.createObjectURL(preview_response.data);
              gallery.items[i]["previewSrc"] = previewSrc;
            }
          } catch (e) {
            console.log(e);
            continue;
          }
        }
        this.thumbnails = gallery;
        this.isLoading = false;
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
  created() {
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
