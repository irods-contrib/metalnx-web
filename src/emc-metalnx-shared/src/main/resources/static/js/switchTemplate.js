$("#adminModeBtn").click(function(){
	$("#adminModeBtn").removeClass("btn-default");
	$("#userModeBtn").removeClass("btn-primary");  
	
	$("#adminModeBtn").addClass("btn-primary");	
	$("#userModeBtn").addClass("btn-default");  	
	
	$.post("/emc-metalnx-web/collections/switchMode/", {currentMode: "user"}, 
		function(response){
			window.location = "/emc-metalnx-web/dashboard/";
		}
	);
});

$("#userModeBtn").click(function(){
	$("#adminModeBtn").removeClass("btn-primary");
	$("#userModeBtn").removeClass("btn-default");
	
	$("#userModeBtn").addClass("btn-primary");
	$("#adminModeBtn").addClass("btn-default");
	
	$.post("/emc-metalnx-web/collections/switchMode/", {currentMode: "admin"}, 
		function(response){
			window.location = "/emc-metalnx-web/collections/";
		}
	);
});