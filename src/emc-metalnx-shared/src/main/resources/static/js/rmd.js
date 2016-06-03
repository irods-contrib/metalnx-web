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

/**
 * Function that renders the disk charts
 * @param jsonDisk
 */	
function createDiskCharts(jsonDisk) {
	var capacity;
	
	$("#table-loader").hide();
	for (var partitionName in jsonDisk) {
		$("#diskSection #partitionsList ul").append(
			"<a href=#hardware-processor-section name=" + partitionName + 
					" onclick=showDiskChart(\'" + partitionName + "\');>" +
				"<li>" + 
					jsonDisk[partitionName]["mounted_on"] + 
					"<i class=\"fa fa-angle-left\"></i>" +
				"</li>" +
			"</a>" +
			"<div class=\"row partitions\" id=" + partitionName + ">" + 
				"<div class=\"col-xs-12 col-md-6\">" +
					"<b>Partition Name</b> <br />" +
					partitionName + "<br />" +
					"<b>Mount Name</b> <br />" +
					jsonDisk[partitionName]["mounted_on"] +
					"<br />" +
				"</div>" +
				"<div class=\"col-xs-12 col-md-6\" id=\"radial_" + partitionName   + "\">" +
					
				"</div>" +
			"</div>"
		);	
		
		$("div[id='" + partitionName + "']").addClass("hideElement");
		
		radialProgress(document.getElementById("radial_" + partitionName))
			.label("Disk Usage")
	    	.diameter(150)
	    	.value(jsonDisk[partitionName]["use_percentage"].slice(0, -1))
	        	.render();
		
		/* 
		 * The df command from the machine does not return a coherent result in terms of
		 * number of used, available and total blocks. To work it around, as we cannot 
		 * specify which are the missing block, we consider it as used. 
		 */
		var used = jsonDisk[partitionName]["blocks"] - jsonDisk[partitionName]["available"];
		
		$("div[id='" + partitionName + "'] div:first-child").append(
			"<br />" +
			"<b>Used:</b> " + byteCountToDisplaySize(used) +
			"<br />" +
			"<b>Free:</b> " + byteCountToDisplaySize(jsonDisk[partitionName]["available"]) +
			"<br />" +
			"<b>Capactity:</b> " + byteCountToDisplaySize(jsonDisk[partitionName]["blocks"]) +
			"<br />"
		);
	}  
}

function showDiskChart(partition){
	$(".partitions").slideUp("slow");
	$(".partitions").addClass("hideElement");
	$("#partitionsList a").each(function(){
		$(this).attr("onclick", "showDiskChart(\'" + $(this).attr("name") + "\');");
	});
	$("#partitionsList i").removeClass("fa-angle-down");	
	$("#partitionsList i").addClass("fa-angle-left");
	
	$("div[id='" + partition + "']").slideDown("slow");
	$("a[name='" + partition + "']").attr("onclick", "hideDiskChart(\'" + partition + "\');");
	$("a[name='" + partition + "'] i").removeClass("fa-angle-left");
	$("a[name='" + partition + "'] i").addClass("fa-angle-down");
}

function hideDiskChart(partition){
	$("div[id='" + partition + "']").slideUp("slow");
	$("a[name='" + partition + "']").attr("onclick", "showDiskChart(\'" + partition + "\');");
	$("a[name='" + partition + "'] i").removeClass("fa-angle-down");	
	$("a[name='" + partition + "'] i").addClass("fa-angle-left");
}

/**
 * Function that displays memory information of a server
 * @param jsonMemory
 * 			json that has all memory information of a server
 */
function displayMemoryInfo(jsonMemory) {
	var usage_percentage = ((jsonMemory["mem"]["used"] / jsonMemory["mem"]["total"]) * 100).toFixed(1);
	var available_percentage = ((jsonMemory["mem"]["free"] / jsonMemory["mem"]["total"]) * 100).toFixed(1);
	
	var total_display = byteCountToDisplaySize(jsonMemory["mem"]["total"] * 1024);
	var used_display = byteCountToDisplaySize(jsonMemory["mem"]["used"] * 1024);
	var avail_display = byteCountToDisplaySize(jsonMemory["mem"]["free"] * 1024);
	var shared_display = byteCountToDisplaySize(jsonMemory["mem"]["shared"] * 1024);
	
	$("#hardware-memory-section #memory-total").html(total_display);
	$("#hardware-memory-section #memory-used").html(used_display + " (" + usage_percentage + "%)");
	$("#hardware-memory-section #memory-free").html(avail_display + " (" + available_percentage + "%)");
	$("#hardware-memory-section #memory-shared").html(shared_display);
	
	$("#hardware-memory-section table").show();
}

/**
 * Function that displays CPU statistics of a server
 * @param jsonCPU
 * 			json that contains all CPU information of a server
 */
function displayCPUStatInfo(jsonCPU){
	var x = "";
	for (var key in jsonCPU) {
		$("#hardware-cpu-section #cpu-user-mode").html(jsonCPU[key]["us"]);
		$("#hardware-cpu-section #cpu-system-mode").html(jsonCPU[key]["sy"]);
		$("#hardware-cpu-section #cpu-idle-task").html(jsonCPU[key]["id"]);
		$("#hardware-cpu-section #cpu-io-waiting").html(jsonCPU[key]["wa"]);
	}   
	$("#hardware-cpu-section table").show();
}

/**
 * Function that displays CPU information of a server
 * @param data
 * 			CPU data to be displayed
 */
function displayCPUInfo(data){
	$("#hardware-processor-section").html(data["model name"]);
}

/**
 * Function that displays iRODS logs
 * @param data
 * 			iRODS log data to be displayed
 */
function displayIRODSLogs(data){
	$("#table-loader").hide();
	$("#irodsServerLogs").html("");
	for (message in data['lines']) {
		$("#irodsServerLogs").append(data['lines'][message]);
		$("#irodsServerLogs").append("<br /><br />");
	}
}

/**
 * Function that shows iRODS status
 * @param data
 * 			status data to be displayed
 */
function displayIRODSStatus(data){
	if (data['status'] == "up") {
		$("#irodsServerLogsPanel").removeClass("panel-danger");
		$("#irodsServerLogsPanel").removeClass("panel-default");
		$("#irodsServerLogsPanel").addClass("panel-success");
	}
	else {
		$("#irodsServerLogsPanel").removeClass("panel-success");
		$("#irodsServerLogsPanel").removeClass("panel-default");
		$("#irodsServerLogsPanel").addClass("panel-danger");
	}
}

/**
 * Function that shows total storage information of the Data Grid
 * @param data
 * 			storage data to be shown
 */
function displayTotalStorageInfo(data){
	var storage = data.split("/");
	var totalStorageUsed = storage[0];
    var totalStorageAvailable = storage[1];
    var totalStorage = storage[2];
	
	$("#totalStorageSum #totalStorageAvailable").html(totalStorageAvailable);
	$("#totalStorageSum #totalStorage").html(totalStorage);
	
	var usagePercentage = storage[3];
	var barType = "success";
	
	if(usagePercentage >= 95 ){
		barType = "danger";
	}
	else if (usagePercentage >= 90) {
		barType = "warning";
	}
	
	$("#totalStorageUsed").html(totalStorageUsed);
    $("#totalStorageAvailable").html(totalStorageAvailable);
    $("#totalStorage").html(totalStorage);
    
    radialProgress(document.getElementById("storageChart"), 120, 160)
    .label("Used")
    .diameter(150)
    .value(usagePercentage)
        .render();
	
	/*$("#totalStorageSum #storageChart").html(
		'<div class="progress">'
		 	+ '<div class="progress-bar progress-bar-' + barType 
		 			+ '" style="width:' + usagePercentage + '%">'
	  		+ '</div>'
		+ '</div>'
	);*/
}

/**
 * Convert a number of bytes into a human readable format
 * @param bytes
 * 			number of bytes to be converted
 */
function byteCountToDisplaySize(bytes) {
	
	var KB = 1024;
    var MB = KB * 1024;
    var GB = MB * 1024;
    var TB = GB * 1024;
    var precision = 1;
    
    bytes *= KB;
   
    if ((bytes >= 0) && (bytes < KB)) {
        return bytes + ' B';
 
    } else if ((bytes >= KB) && (bytes < MB)) {
        return (bytes / KB).toFixed(precision) + ' KB';
 
    } else if ((bytes >= MB) && (bytes < GB)) {
        return (bytes / MB).toFixed(precision) + ' MB';
 
    } else if ((bytes >= GB) && (bytes < TB)) {
        return (bytes / GB).toFixed(precision) + ' GB';
 
    } else if (bytes >= TB) {
        return (bytes / TB).toFixed(precision) + ' TB';
 
    } else {
        return bytes + ' B';
    }
}