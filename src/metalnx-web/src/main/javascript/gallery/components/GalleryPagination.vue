<template>
  <div class="gallery-pagination-container">
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
      <b-form-select-option :value="null" disabled
        >-- Select thumbnail size --</b-form-select-option
      >
    </b-form-select>
    <b-form-select
      v-model="items_selected"
      class="thumbnail-items-select"
      :options="items_options"
      @change="updateItemPerPage(items_selected)"
    >
      <b-form-select-option :value="null" disabled
        >-- Select items per page --</b-form-select-option
      >
    </b-form-select>
  </div>
</template>

<script>
export default {
  name: "GalleryContextProvider",
  components: {},
  props:['currPerPage','currPage','currSize'],
  data() {
    return {
      size_options: ["small", "medium", "large"],
      items_options: [10, 25, 50, 75, 100],
    };
  },
  methods: {
    linkGen(pageNum) {
      return `/metalnx/gallery?path=${encodeURIComponent(
        this.currPath
      )}&page=${pageNum}&perPage=${this.currPerPage}&size=${this.currSize}`;
    },
    updateItemPerPage(newPerPage) {
      window.location = `/metalnx/gallery?path=${encodeURIComponent(
        this.currPath
      )}&page=${this.currPage}&perPage=${newPerPage}&size=${this.currSize}`;
    },
    updateThumbnailSize(newSize) {
      window.location = `/metalnx/gallery?path=${encodeURIComponent(
        this.currPath
      )}&page=${this.currPage}&perPage=${this.currPerPage}&size=${newSize}`;
    },
  },
};
</script> 
<style>
.gallery-pagination-container {
  display: flex;
  flex-direction: row;
}

.gallery-nav {
  margin: 10px 10px 10px 30px;
}

.thumbnail-size-select {
  margin-top: 10px;
  margin-left: 10px;
  width: 100px;
}

.thumbnail-items-select {
  margin-top: 10px;
  margin-left: 10px;
  width: 300px;
}
</style>
