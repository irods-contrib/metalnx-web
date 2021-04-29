<template>
  <span>
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
</template>

<script>
export default {
  name: "GalleryWidgets",
  components: {},
  props: ['url'],
  data() {
    return {};
  },
};
</script> 
<style>
</style>
