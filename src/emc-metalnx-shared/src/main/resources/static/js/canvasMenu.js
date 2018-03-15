$(document).ready(function(){
  	$("#close-sidecanvas").click(function(){
  		$('.sideCanvas').hide();
  	});
      
});

function getSideCanvas(path){
	var url = "/emc-metalnx-web/browse/summary";		 
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "POST", {path: path}, displaySideCanvas, null, null);
}

function displaySideCanvas(data){
	$('.sideCanvas').html(data);
	$('.sideCanvas').appendTo("#page-nav-wrapper");
	$(".sideCanvas").show();
}
