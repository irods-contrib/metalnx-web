<template>
  <div class="gallery-item">
    <a
      :id="'thumbnail' + item.id"
      v-if="item.collection"
      :href="`/metalnx/gallery?path=${path}%2F${item.name}`"
    >
      <i class="fa fa-folder fa-3x" />
      <div class="gallery-item_name">{{ item.name }}</div>
    </a>
    <a
      :id="'thumbnail' + item.id"
      v-else
      :href="`/metalnx/collectionInfo?path=${path}%2F${item.name}`"
    >
      <i class="fa fa-file fa-3x" />
      <div class="gallery-item_name">{{ item.name }}</div>
    </a>
    <b-tooltip :target="'thumbnail' + item.id" triggers="hover">
      <div>File Name: {{ item.name }}</div>
    </b-tooltip>
  </div>
</template>

<script>
export default {
  name: "GalleryItem",
  components: {},
  props: ["item", "path"],
  data() {
    return {};
  },
};
</script> 
<style>
.gallery-item {
  display: flex;
  flex-direction: column;
  width: 200px;
  height: 200px;
  justify-content: center;
  text-align: center;
}

.gallery-item_name {
  width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
