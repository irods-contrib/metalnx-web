<template>
  <div class="gallery-item">
    <a
      :id="'thumbnail' + item.id"
      v-if="item.collection"
      class="gallery-item_anchor"
      :href="`/metalnx/gallery?path=${path}%2F${item.name}`"
    >
      <div class="gallery-thumbnail"><i class="fa fa-folder fa-3x" /></div>
      <div class="gallery-item_name">{{ item.name }}</div>
    </a>
    <a
      :id="'thumbnail' + item.id"
      v-else
      class="gallery-item_anchor"
      :href="`/metalnx/collectionInfo?path=${path}%2F${item.name}`"
    >
      <div class="gallery-thumbnail">
        <img
          alt="Thumbnail Image"
          :src="item.previewSrc"
          class="gallery-thumbnail"
        />
      </div>
      <div class="gallery-item_name">{{ item.name }}</div>
    </a>
    <b-tooltip :target="'thumbnail' + item.id" triggers="hover">
      <div>Name: {{ item.name }}</div>
      <div>{{ item.hover }}</div>
    </b-tooltip>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "GalleryItem",
  components: {},
  props: ["item", "path"],
  data() {
    return {};
  },
  // methods: {
  //   async fetchPreview() {
  //     let preview_prep_response = await axios({
  //       method: "GET",
  //       url: "/metalnx/previewPreparation/",
  //       params: {
  //         path: `${this.item.thumbnail}`,
  //       },
  //     });
  //     let preview_response = await axios({
  //       method: "GET",
  //       url: "/metalnx/preview/dataObjectPreview/",
  //       responseType: 'blob'
  //     });
  //     this.preview_response = preview_response;
  //     console.log(preview_response);
  //     this.previewSrc = URL.createObjectURL(preview_response.data);
  //   },
  // },
  // mounted() {
  //   this.fetchPreview();
  // },
};
</script> 
<style>
.gallery-item {
  display: flex;
  flex-direction: column;
  width: 150px;
  height: 200px;
  margin: 10px;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.gallery-item_name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 150px;
}

.gallery-item_anchor:hover {
  text-decoration: none;
}

.gallery-thumbnail {
  height: 150px;
  width: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  object-fit: scale-down;
}
</style>
