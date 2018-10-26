function getAllNotifications(path){
	
	console.log("getInfoDetails()");
	//window.location.hash = "info";
	var url = "/emc-metalnx-web/notification/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
}