<template>
  <div class="gallery-path-container">
    <span :key="path" v-for="(path, index) in pathArray">
      <i
        v-if="index !== 0"
        class="fa fa-angle-double-right path gallery-right-shift"
      ></i
      ><a class="gallery-path" v-bind:href="generateUrl(pathArray, path)">
        {{ path }}
      </a>
    </span>
  </div>
</template>

<script>
export default {
  name: "GalleryContextProvider",
  components: {},
  props: ["pathArray"],
  data() {
    return {};
  },
  methods: {
    generateUrl(paths, path) {
      let index = paths.indexOf(path);
      let url = "/metalnx/gallery?path=";
      for (let i = 0; i <= index; i++) {
        url += encodeURIComponent("/" + paths[i]);
      }
      return url;
    },
  },
};
</script> 
<style>
.gallery-path-container {
  width: 60%;
}
.gallery-right-shift {
  padding-left: 5px;
  padding-right: 5px;
}
.gallery-path {
  color: #287ea1;
}
</style>
