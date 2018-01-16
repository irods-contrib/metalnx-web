function getCollectionSummary(path){
	console.log(" In getTestInfo() " +path);
	var url = "/emc-metalnx-web/collectionInfo"+path;
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayCollectionSummary, null, null);
}

function displayCollectionSummary(data){
	alert("displayTestDetails()");	
	$("#summary").html(data);
	//alert([[${ currentPath }]]);
	getInfoDetails(path);
}


function getInfoDetails(path){
	console.log("Collection getInfoDetails() starts !!");
	window.location.hash = "info";
	console.log("Path :: " +path);
	//var url = "/emc-metalnx-web/collectionInfo/collectionFileInfo/";
	var url = "/emc-metalnx-web/collections/info/";		
	//getBreadcrumb(path);
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
	
}

function getMetadata(path){
	console.log("Collection getMetadata() :: " +path);
	window.location.hash = "metadata";
	console.log("window.location.hash :: " +window.location.hash);
	var url = "/emc-metalnx-web/metadata/getMetadata/";	
	console.log("url " +url);	
	//getBreadcrumb(path);	
	ajaxEncapsulation(url , "POST", {path: path}, displayMetadata, null, null, null);
}

function getPermissionDetails(path){
	console.log("Collection getPermDetails() :: " +path);
	window.location.hash = "permission";
	var url = "/emc-metalnx-web/permissions/getPermissionDetails/";
	console.log("URL :: " +url);
	//getBreadcrumb(path);
	ajaxEncapsulation(url, "POST", {path: path}, displayPermissionDetails, null, null);
}


function displayInfoDetails(data){
	$("#info").html(data);
}

function displayMetadata(data){
	$("#metadata").html(data);	
}

function displayPermissionDetails(data){
	$('#permission').html(data);
    
}

/*

function ChangeUrl(title, urlVal) {
	console.log("ChangeUrl()");
	var str1 = "emc-metalnx-web/collections";	
	var url = str1.concat(urlVal);	
	console.log("url :: " +url);	

    if (typeof (history.pushState) != "undefined") {
        var obj = { Title: title, Url: url };
        history.pushState(obj, obj.Title, obj.Url);
    } else {
        alert("Browser does not support HTML5.");
    }
}*/

