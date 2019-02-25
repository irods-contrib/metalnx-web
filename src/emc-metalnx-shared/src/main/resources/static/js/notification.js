function getAllNotifications(path){
	
	console.log("getInfoDetails()");
	//window.location.hash = "info";
	var url = "/metalnx/notification/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
}