$(document).ready(function(){
  	$("#close-sidecanvas").click(function(){	
  		$(".side-canvas").detach();
  	});
      
});

function getSideCanvas(path,name){
	
	var cls = '.'+name	
	$(cls).parent('td').parent('tr').addClass('panel-error'); //to change the color of selected line
	
	var url = "/metalnx/browse/summary";		 
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "POST", {path: path}, displaySideCanvas, null, null);
}

function displaySideCanvas(data){		
	$(".side-canvas").detach();	
	console.log("data :: " +data);
	$("#page-nav-wrapper").append(data);
	
}

function showMetadata(path){
	console.log("showMetadata :: " +path);
	var url = "/metalnx/metadata/getMetadata/";
	ajaxEncapsulation(url , "POST", {path: path}, displayMetadataSnapshot, null, null, null);
	
}

function displayMetadataSnapshot(data){
	$('#snapshotMetadataModal').modal(); 
	$("#snapshotMetadata").html(data);
}

