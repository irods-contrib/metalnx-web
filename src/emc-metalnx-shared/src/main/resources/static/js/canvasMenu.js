$(document).ready(function(){
  	$("#close-sidecanvas").click(function(){		
  		$('.side-canvas').hide();
  	});
      
});

function getSideCanvas(path){
	var url = "/emc-metalnx-web/browse/summary";		 
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "POST", {path: path}, displaySideCanvas, null, null);
}

function displaySideCanvas(data){	
	$('.side-canvas').html(data);
	//$('.side-canvas').remove();
	$('.side-canvas').appendTo("#page-nav-wrapper");
	$('.side-canvas').show();
}
