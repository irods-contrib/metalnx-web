function getCollectionSummary(path){	
	var url = "/emc-metalnx-web/collectionInfo"+path;
	getBreadcrumb(path);
	console.log("URL :: " +url);
	ajaxEncapsulation(url, "GET", {path: path}, displayCollectionSummary, null, null);
}

function displayCollectionSummary(data){
	console.log("displayTestDetails()");	
	$("#summary").html(data);	
	//displayInfoDetails(data);	
}


function getInfoDetails(path){
	$("#info").hide();
	$("#table-loader").show();	
	window.location.hash = "info";	
	var url = "/emc-metalnx-web/collectionInfo/collectionFileInfo/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
	
}

function getMetadata(path){
	$("#metadata").hide();
	$("#table-loader").show();		
	console.log("Collection getMetadata() :: " +path);
	window.location.hash = "metadata";
	var url = "/emc-metalnx-web/metadata/getMetadata/";	
	ajaxEncapsulation(url , "POST", {path: path}, displayMetadata, null, null, null);
}

function getPermissionDetails(path){
	$("#permission").hide();
	$("#table-loader").show();		
	console.log("Collection getPermDetails() :: " +path);
	window.location.hash = "permission";
	var url = "/emc-metalnx-web/permissions/getPermissionDetails/";	
	ajaxEncapsulation(url, "POST", {path: path}, displayPermissionDetails, null, null);
}


function displayInfoDetails(data){
	$("#table-loader").hide();
	$("#info").html(data);
	$("#info").show();
}

function displayMetadata(data){
	$("#table-loader").hide();	
	$("#metadata").html(data);	
	$("#metadata").show();
}

function displayPermissionDetails(data){
	$("#table-loader").hide();
	$('#permission').html(data);
	//alert('showing content menu');
	$("#permission").show();    
}

function showPreview(){
	alert("Show Preview");
	
	$.ajax({
		type : "POST",
		url : "/emc-metalnx-web/collectionInfo/getFile/",
		timeout : 100000,
		success : function(data) {
			console.log("SUCCESS: ", data);
			console.log(data);
		},
		error : function(e) {
			console.log("ERROR: ", e);
			console.log(e);
		},
		done : function(e) {
			console.log("DONE");
		}
	});
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

