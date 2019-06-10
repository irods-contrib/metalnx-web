 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

$("#adminModeBtn").click(function(){
	$("#adminModeBtn").removeClass("btn-default");
	$("#userModeBtn").removeClass("btn-primary");

	$("#adminModeBtn").addClass("btn-primary");	
	$("#userModeBtn").addClass("btn-default");

	$.post("/metalnx/browse/switchMode/", {currentMode: "user"},
		function(response){
			window.location = "/metalnx/dashboard/";
		}
	);
});

$("#userModeBtn").click(function(){
	$("#adminModeBtn").removeClass("btn-primary");
	$("#userModeBtn").removeClass("btn-default");

	$("#userModeBtn").addClass("btn-primary");
	$("#adminModeBtn").addClass("btn-default");

	$.post("/metalnx/collections/switchMode/", {currentMode: "admin"},
		function(response){
			window.location = "/metalnx/collections/";
		}
	);
});
