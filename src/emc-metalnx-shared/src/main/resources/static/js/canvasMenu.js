$(document).ready(function(){

	/*$('#open-left-canvas').click(function(){
		$('.left-canvas').toggle();
	});*/
	
	/* $('#treeViewTable').on('click', 'tr',  function () {
		 	var path = "/zone1/home/test1";
		 	var url = "/emc-metalnx-web/collectionInfo"+path;		 
			getBreadcrumb(path);
			console.log("URL :: " +url);
			ajaxEncapsulation(url, "GET", {path: path}, displayMenu, null, null);
     	
     });*/

	$('#close-left-canvas').click(function(){
		$('.left-canvas').hide();
	});

});

function getCanvasSummary(path){
	//var path = "/zone1/home/test1";
 	var url = "/emc-metalnx-web/browse/summary;		 
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayMenu, null, null);
}

function displayMenu(){
	$('.left-canvas').show();
}
