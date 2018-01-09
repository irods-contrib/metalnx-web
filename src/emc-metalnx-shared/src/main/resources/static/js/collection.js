function getTestInfo(path){
	console.log(" In getTestInfo() " +path);

	var url = "/emc-metalnx-web/collectionInfo"+path;
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayTestDetails, null, null);
}

function displayTestDetails(data){
	console.log("displayTestDetails()");
	$('#table-loader').hide();
	$('#table-loader').after(data);
}
function getPermDetails(path){
	console.log("Collection getPermDetails() :: " +path);
	var url = "/emc-metalnx-web/collectionInfo"+path;
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayPermDetails, null, null);
}

function getInfoDetails(path){
	console.log("Collection getInfoDetails() :: " +path);

	var url = "/emc-metalnx-web/collectionInfo/collectionFileInfo"+path;
	console.log("URL :: " +url);
	getBreadcrumb(path);
	ajaxEncapsulation(url, "GET", {path: path}, displayInfoDetails, null, null, null);
}

function getMetadata(path){
	console.log("Collection getMetadata() :: " +path);
	window.location.hash = "metadata";
	console.log("window.location.hash :: " +window.location.hash);
	var url = "/emc-metalnx-web/collectionInfo/collectionMetadata"+path;
	console.log(1);	
	getBreadcrumb(path);	
	ajaxEncapsulation(url, "GET", {path: path}, displayTestDetails, null, null);
}

/*function getInfoDetails(path){
	console.log("Collection getInfoDetails() :: " +path);
	window.location.hash = "info";
	console.log("window.location.hash :: " +window.location.hash);
	var url = "/emc-metalnx-web/collectionInfo/collectionFileInfo"+path;
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayTestDetails, null, null);
	
}

function getPermDetails(path){
	console.log("Collection getPermDetails() :: " +path);
	window.location.hash = "permission";
	console.log("window.location.hash :: " +window.location.hash);
	var url = "/emc-metalnx-web/collectionInfo/collectionPermisssionDetails"+path;
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayTestDetails, null, null);
	
}


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