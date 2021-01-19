<template>
  <!-- Search -->
  <div>
  <div class="widgetsContainer">
    <span>{{formattedPath}}</span>
    <span class="widgets">
    <b-form-select v-model="selected" style=" width: 140px;" :options="options">
    </b-form-select>
    <b-link href="#" class="btn btn-default icon-btn"
				id="showCollectionFormBtn" aria-label="add collection"
				title="add collection"> <i
				class="fa fa-folder"></i>
			</b-link>
    <b-link class="btn btn-default icon-btn"
				:href="`/metalnx/collectionInfo${url}`"
				id="infoIcon"
				title="View info"><i class="fa fa-info-circle fa-color"></i>
			</b-link>
      <b-link href="#" class="btn btn-default icon-btn" aria-label="upload files" id="uploadIcon"
				title="Upload file(s) to iRods"> <i
				class="fa fa-cloud-upload fa-color"></i>
			</b-link>
    <b-link :href="`/metalnx/collections${url}`"
    class="btn btn-default icon-btn"
    id="collectionIcon"
    title="Collection List View"><i class="fa fa-list fa-lg" aria-hidden="true"></i></b-link>
    </span>
  </div>
    <div class="galleryContainer">
      <div class="gallery" v-for="item in demo.items">
        <a :id="'thumbnail'+item.id" :href="`/metalnx/collectionInfo${url}%2F${item.name}`">
        <img v-bind:class="[selected]" :src="item.thumbnails" />
        <div class="thumbnail_name">{{ item.name }}</div>
        </a>
        <b-tooltip :target="'thumbnail'+item.id" triggers="hover">
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
    let url = window.location.search;
    let params = new URLSearchParams(url.substring(1));
    let path = decodeURIComponent(url.substring(9));
    let formattedPath = path.replaceAll("/", " >> ")

    let response = axios({
      method: 'GET',
      url: 'http://localhost/metalnx/api/gallery',
      params:{
        path: '/tempZone/home/test1',
        offset: 0,
        limit: 100
      }
    }).catch(e => console.log(e));

    var demo = {
      location: "testLocation",
      items: [
        {
          id: 1,
          name: "sample1.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample1.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 2,
          name: "sample2.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample2.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 3,
          name: "sample3.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample3.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 4,
          name: "sample4.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample7.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 5,
          name: "sample5.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample5.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 6,
          name: "sample6.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample6.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 7,
          name: "sample7.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample7.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 8,
          name: "sample8.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample8.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 9,
          name: "sample9.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample9.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
        {
          id: 10,
          name: "sample10.png",
          lastModified: "2020-12-14",
          thumbnails: "./img/sample10.png",
          fileSize: "10 MB",
          fileType: "Portable Network Graphics"
        },
      ],
    };
    return {
      demo,
      url,
      formattedPath,
      response,
      selected: "medium",
      options: ["small", "medium", "large"]
    };
  },
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
  
  .small{
	width: 75px;
	height: 75px;
  }
  
  .thumbnail_select{
	width: 100px;
	margin: 20px 10px auto 40px;
  }
  
  .gallery{
	margin: 10px 20px 10px 20px;
	text-align: center;
  }
  
  .thumbnail_name{
	margin:5px auto 5px auto;
  }

  .collection_view{
    float: right;
    margin-top: 25px;
    margin-right: 140px;
  }

  .widgetsContainer{
    margin-top: 20px;
    margin-left: 20px;
    display:'flex';
    flex-direction: column;
  }

  .widgets{
    margin-left: 35%;
  }

</style>
