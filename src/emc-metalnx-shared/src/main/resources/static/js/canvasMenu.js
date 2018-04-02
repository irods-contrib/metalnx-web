$(document).ready(function(){
  	$("#close-sidecanvas").click(function(){	
  		$(".side-canvas").detach();
  	});
      
});

function getSideCanvas(path){
	var url = "/emc-metalnx-web/browse/summary";		 
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "POST", {path: path}, displaySideCanvas, null, null);
}

function displaySideCanvas(data){		
	$(".side-canvas").detach();	
	console.log("data :: " +data);
	$("#page-nav-wrapper").append(data);
	
}
