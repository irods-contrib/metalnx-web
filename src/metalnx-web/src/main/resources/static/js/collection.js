function getCollectionSummary(path){
	window.location.href = '/metalnx/collectionInfo?path=' + path; //relative to domain
}

function displayCollectionSummary(data){
	$("#summary").html(data);
}

function getInfoDetails(path){
	var url = "/metalnx/collectionInfo/collectionFileInfo/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
}

function getMetadata(path){
	console.log("Collection getMetadata() :: " +path);
	var url = "/metalnx/metadata/getMetadata/";
	ajaxEncapsulation(url , "POST", {path: path}, displayMetadata, null, null, null);
}

function getPermissionDetails(path){
	console.log("Collection getPermDetails() :: " +path);
	var url = "/metalnx/permissions/getPermissionDetails/";
	ajaxEncapsulation(url, "POST", {path: path}, displayPermissionDetails, null, null);
}

function getPreview(path){
	console.log("Collection getPreview() :: " + path)
	var url = "/metalnx/previewPreparation/";
	ajaxEncapsulation(url, "GET", {path:path}, displayPreviewImage, null, null);
}

function displayInfoDetails(data){
	$("#details").html(data);
}

function displayMetadata(data){
	/*$("#table-loader").hide();
	$("#metadata").show();*/
	$("#metadata").html(data);
}

function displayPermissionDetails(data){
	/*$(".loading").hide();
	$("#permission").show();*/
	$('#permission').html(data);
}

function displayPreviewImage(data){
	$("#preview").html(data);
}


function starPath(path){
	console.log("StarPath() starts");
	//$('#breadcrumbStar').attr('onclick', '');
	var url = "/metalnx/favorites/addFavoriteToUser/";
	ajaxEncapsulation(url, "GET", {path: path},
			function(data){
		if(data.indexOf("OK") >= 0){
			$('#breadcrumbStar i').removeClass('bm-unchecked').addClass('bm-checked');
			$('#breadcrumbStar').attr('onclick', 'unstarPath("'+encodeURIComponent(path)+'")');
			//$('#breadcrumbStar').tooltip('hide').attr('data-original-title',[[#{collections.favorite.unmark.button.tooltip}]]);
		}else{
			$('#breadcrumbStar').attr('data-content', 'Could not add path to favorites.')
			$('#breadcrumbStar').popover("show");
			$('#breadcrumbStar').attr('onclick', 'starPath("'+encodeURIComponent(path)+'")');
		}

	}, null, null, null);
	console.log("StarPath() ends");
}

function unstarPath(path){
	console.log("UnstarPath() starts !!");
	//$('#breadcrumbStar').attr('onclick', '');
	var url = "/metalnx/favorites/removeFavoriteFromUser/";
	ajaxEncapsulation(url, "GET", {path: path},
		function(data){
			if(data.indexOf("OK") >= 0){
				$('#breadcrumbStar i').removeClass('bm-checked').addClass('bm-unchecked');
				$('#breadcrumbStar').attr('onclick', 'starPath("'+encodeURIComponent(path)+'")');
				//$('#breadcrumbStar').tooltip('hide').attr('data-original-title',[[#{collections.favorite.button.tooltip}]]);
			}else{
				$('#breadcrumbStar').attr('data-content', 'Could not remove path from favorites.')
				$('#breadcrumbStar').popover("show");
				$('#breadcrumbStar').attr('onclick', 'unstarPath("'+encodeURIComponent(path)+'")');
			}
		}, null, null, null);
	console.log("UnstarPath() ends");
}

/*
path should be urlencoded already
*/
function positionBrowserToPath(path) {
	console.log("positionBrowserToPath()");
	window.location.href = '/metalnx/collections?path=' + encodeURIComponent(path); //relative to domain
}

function fileDownload(path){
	$("#breadcrumDownloadBtn").attr('disabled','disabled');
	$("#actionsWait").show();
	//$('#actionLabel').html([[#{collections.management.progress.label.download}]]);
	$('#actionLabel').text($("#container").data("msg-txt"));
	var prepareDownloadURL = "/metalnx/fileOperation/prepareFilesForDownload/";
	var paths = [];
	paths.push(path);
	ajaxEncapsulation(prepareDownloadURL, "GET", {paths: paths}, handleDownload, null);
}

function showInheritanceAction() {
	$("#updateInheritanceModal").modal("show");
}

function updateInheritanceNonRecursive(path) {
	updateInheritance(path, false);
}

function updateInheritanceRecursive(path) {
	updateInheritance(path, true);
}

function updateInheritance(path, isRecursive) {
	console.log("updateInheritance()");
	console.log("path:" + path);
	console.log("recursive:" + isRecursive);
	var inheritanceValue = ($('#inheritCheck').is(':checked'));
	console.log("inheritanceValue:" + inheritanceValue);

	var inheritanceUrl = "/metalnx/inheritance";
	setOperationInProgress();
	ajaxEncapsulation(inheritanceUrl, "POST", {path: path, recursive: isRecursive, inherit: inheritanceValue}, inheritanceSuccessful(), null);
}

function inheritanceSuccessful() {
	toastr["success"]("Operation successful", "inheritance value was updated successfully");
	unsetOperationInProgress();
}


function deleteInfoAction(path){
	setOperationInProgress();
	console.log("Ready for deletion");
	$("#actionmenu button").prop("disabled", true);
	$('#actionsWait').show();

	var paths = [];
	paths.push(path);
	var url = "/metalnx/fileOperation/deleteNoRedirect/";

	ajaxEncapsulation(
			url,
			"POST",
			{paths: paths},
			function(failedDeletions) {
				unsetOperationInProgress();
				$('#actionsWait').hide();
				
				// The delete operation failed if the array is not empty.
				// The failure is likely permissions related.
				if (failedDeletions.length > 0) {
					$('#deleteFailureModal').modal();
					$("#actionmenu button").prop("disabled", false);
				}
				else {
					window.location.href = "browse/home";
				}
			}
	);
	$("#deleteModal").modal("hide");
	cleanModals();
}

function cleanModals() {
	$(".modal").modal("hide");

	//reset the modal
	$(".modal a").each(function(){
		if($(this).attr('onclick') !== undefined){
			if($(this).attr('onclick').indexOf('retractElement') >= 0){
				eval($(this).attr('onclick'));
			}
		}
	});
}

function editInfo(path){

}

function handleDownload(data) {
	console.log("collection.js :: success call :: handleDownload()")
	if (data.downloadLimitStatus == "ok"){
		window.location.href = "/metalnx/fileOperation/download/";
		$("#breadcrumDownloadBtn").removeAttr("disabled");
		$("#actionsWait").hide();
	}
	else {
		toastr.warning("Download size exceeds the limit (" + data.downloadLimitInMB + "MB)");
		$("#actionsWait").hide();
	}
}

function setOperationInProgress() {
	operationInProgress = true;
}

function unsetOperationInProgress() {
	cleanModals();
	operationInProgress = false;
}

function accessRequest(path){
	var url = "/emc-metalnx-web/collectionInfo/accessRequest";
	ajaxEncapsulation(url, "GET", {path: path}, loadEmailResponse, null, null, null);
}
function loadEmailResponse(data){
	$("#readOnlyData").hide();
	$("#responseTxt").text(data);
	$("#emailResponse").show();

}
