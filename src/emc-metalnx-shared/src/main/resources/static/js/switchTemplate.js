/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

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