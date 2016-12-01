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
 * JS file used for implementing the bulk upload function.
 */
var applyToCurrentPath = false;
var chunkSize = 1024 * 1024; //1MB
var partSize = 4 * 1024 * 1024; //4MB
var chunksSent = 0;
var files;
var resolvedFileNames = [];
var sizeAllFiles = 0;
var percentSizeAllFiles = 0;
var originalPagetitle = $('title').html();

/**
 * Function that checks when the users selects files for upload.
 */

$("input[name='files']").change(function () {
	resolvedFileNames = [];
	
	files = $("input[name='files']").prop("files");
	$('#numberFilesUpload').html(files.length);
	$('#browseButton').hide();
	
	$.each(files, function(index, file){
		var fileName = resolveFileName(file.name);
		$("#filesList").append('<p>' + fileName + '</p>');
		resolvedFileNames.push(fileName);
	});

	$('#uploadControlOptions').show();
});


/**
 * Handles onclick event when the user clicks on uploading a set of files.
 */	        
$("#uploadButton").click(function(){
	if($("input[name='files']").prop("files").length == 0 ){
		$('#uploadMinMessage').show();
		return;
	}

	setOperationInProgress();

	$('.progress-bar.progress-bar-striped.active').css('width', '0%');
	$('.progress-bar.progress-bar-striped.active').attr('aria-valuenow', 0);
	$('.progress-bar.progress-bar-striped.active').html('0%');
	$('#uploadModal').modal('hide');
	$('#uploadIcon').hide();
	$('#showCollectionFormBtn').hide();
	$('#panelUpload').show();
	$('#uploadStatusIcon .badge').html(files.length);
	
	var uploadItems = "";
	var preparedFiles = 0;
	
	sizeAllFiles = 0;
	percentSizeAllFiles = 0;
	
	$.each(files, function(index, file){
		var fileName = resolvedFileNames[index];
		var fileSize = file.size;
		var totalParts = Math.ceil(fileSize / partSize);
		var totalChunksPerPart = Math.ceil(partSize / chunkSize);
		var totalChunks = Math.ceil(fileSize/chunkSize);
		var url = "/emc-metalnx-web/upload/prepare/";

		var preUploadData = {
            fileName : fileName,
            path : $('#navigationInput').val(),
            fileSize : fileSize,
            partSize : partSize,
            totalParts : totalParts,
            chunkSize : chunkSize,
            totalChunksPerPart : totalChunksPerPart,
            totalChunks: totalChunks,
            checksum : $('#inputChecksum').is(':checked'),
            replicate : $('#inputReplica').is(':checked'),
            destResc : $('#selectResourceToUpload').val(),
            replResc : $('#selectResource').val(),
            overwrite : $('#inputOverwriteDuplicateFiles').is(':checked')
        }

		sizeAllFiles += fileSize;
		
		$('#uploadStatusIcon').removeClass('hide');
		$('#uploadStatusIcon ul.dropdown-menu').empty();

		ajaxEncapsulation(
			url, 
			"POST", 
			preUploadData,
			function () {
			    preparedFiles++;
			    if (preparedFiles == files.length) {
			        sendFilePart(files, 0, 0, 0);
			    }
			}, 
			null, 
			null, 
			null
		);

		uploadItems += '<li id="'+index+'"><a class="col-sm-12">'+
		'<input type="hidden" class="paused" value="false" />'+
		'<div class="col-sm-4" style="float:left; margin-right:10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">'+
		'<span style="text-align:right" title="' + fileName + '">' + fileName + ' </span>'+
		'</div>'+
		'<div class="col-sm-6 progressWrapper">'+
		'<div class="progress" style="">'+
		'<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">0%</div>'+
		'</div>'+
		'</div>'+
		'<div class="col-sm-1 progressAction">'+
		'<button type="button" class="btn btn-default btn-xs" onclick="pause(' + index + ', \'' + fileName + '\');">'+
		'<span class="glyphicon glyphicon-pause" aria-hidden="true"></span>'+
		'</button>'+
		'</div>'+
		'</a></li>' ;

	});


	$('#uploadStatusIcon ul.dropdown-menu').html(uploadItems);
});

/**
 * Function that sets the pause flag to true to enable users to stop a current upload.
 */
function pause(index, fileName){
	$('#'+index+' .paused').val("true");
	$('#'+index+' .progressAction .btn.btn-default.btn-xs').attr('onclick', 'getWhereUploadStopped('+index+', \''+fileName+'\')');
	$('#'+index+' .progressAction .btn.btn-default.btn-xs span.glyphicon').removeClass('glyphicon-pause');
	$('#'+index+' .progressAction .btn.btn-default.btn-xs span.glyphicon').addClass('glyphicon-play');
}

/**
 * Function that is responsible for breaking the file in parts and send it to the server.
 * @param file
 *	file to be sent
 * @param currPart
 *	current part is upload is currently sending to the server
 */
function sendFilePart(files, index, currPart, currFilePos){
	if(currFilePos >= files.length) {
		return false;
	}
	
	var startByte = 0;
	var endByte = chunkSize;
	var isEOF = false;
	var currentChunkNumber = 0;
	var startByteForPart = currPart * partSize;
	var endByteForPart = startByteForPart + partSize;
	var file = files[currFilePos];
	var filePart = file.slice(startByteForPart, endByteForPart);
	var fileName = resolvedFileNames[currFilePos];
	var totalParts = Math.ceil(file.size / partSize);
	var reader = new FileReader();
	var crcValue;
	
	reader.onloadend = function(){
		var bytes = reader.result;
		crcValue = crc32_compute_buffer(3988292384, bytes);
		while(!isEOF) {
			var formData = new FormData();		        			
			formData.append('fileName', fileName);
			formData.append('filePart', currPart);
			formData.append('chunkNumber', currentChunkNumber);
			formData.append('partCRC32', crcValue);

			//last file chunk
			if(endByte >= filePart.size) {
				endByte = filePart.size;	
				isEOF = true;
			}

			//start byte until end - 1 (chunking file parts)
			formData.append('fileChunk', filePart.slice(startByte, endByte));

			startByte = endByte;
			endByte += chunkSize;	

			currentChunkNumber++;
			
			// Bind POST parameters to the HTTP request
			$.ajax({
				url:'/emc-metalnx-web/upload/',
				type:'POST',
				data: formData,
				cache: false,
				contentType: false,
				processData: false,
				success:  function(res) {
				    var response = $.parseJSON(res);
				    var path = response.path;
				    var msg = response.msg;

                    chunksSent++;

                    var percentComplete = ((chunksSent*chunkSize)+(currPart*partSize)) / file.size;
                    var progressValue = Math.round(percentComplete*100);

                    if(file.size > chunkSize) {
                        percentSizeAllFiles += (chunkSize/sizeAllFiles)*100;
                    }
                    else {
                        progressValue = 100;
                        percentSizeAllFiles += (file.size/sizeAllFiles)*100;
                    }

                    showProgressBarForFile(index, progressValue);

                    updatePageTitle(progressValue, fileName, Math.round(percentSizeAllFiles));

                    if(progressValue >= 99) showTransferFileIRODSMsg(index);

                    var totalChunksPerParts;
                    if(endByteForPart > file.size) {
                        endByteForPart = file.size;
                        totalChunksPerParts = Math.ceil((file.size % partSize)/chunkSize);
                    }
                    else {
                        totalChunksPerParts = Math.ceil(filePart.size/chunkSize);
                    }

                    //sending the next parts
                    if(file.size > partSize && chunksSent >= totalChunksPerParts && (currPart+1) < totalParts && ($('#' + index + ' .paused').val().indexOf('false') >= 0)) {
                        chunksSent = 0;
                        sendFilePart(files, index, ++currPart, currFilePos);
                    }

                    //sending the next files
                    if(currPart == totalParts - 1 && (currFilePos+1) < files.length && chunksSent >= totalChunksPerParts) {
                        showTransferCompletedMsg(index, msg);
                        chunksSent = 0;
                        currFilePos++;
                        sendFilePart(files, ++index, 0, currFilePos);
                    }
                    else if(currPart == totalParts - 1 && (currFilePos+1) == files.length && chunksSent >= totalChunksPerParts){
                        showTransferCompletedMsg(index, msg);
                        getSubDirectories(path);
                        unsetOperationInProgress();
                        $('title').html(originalPagetitle);
                    }
				},
				error: function(xhr, status, error){
				    var error_response = $.parseJSON(xhr.responseText);

				    showUploadErrorMsg(index, error_response.msg, error_response.errorType);

				    //sending the next files
                    if((currFilePos+1) < files.length) {
                        chunksSent = 0;
                        currFilePos++;
                        sendFilePart(files, ++index, 0, currFilePos);
                    }
                    else if((currFilePos+1) == files.length) {
                        resolvedFileNames = [];
                        getSubDirectories(error_response.path);
                        unsetOperationInProgress();
                    }
				},
				statusCode: {
					408: function(response){
						window.location= "/emc-metalnx-web/login/";
					},
					403: function(response){
						window.location= "/emc-metalnx-web/login/";
					}
				}
			});
		} //end for (chunks)
	};
	reader.readAsArrayBuffer(filePart);

}

var uploadSuccess = 'success';
var uploadWarning = 'warning';
var uploadDanger = 'danger';

// shows the upload error with the appropriate layout
function showUploadErrorMsg(fileId, errorMsg, type) {
    var id = '#' + fileId + ' .progressWrapper';

    var icon = type.indexOf(uploadWarning) != -1 ? 'ok' : 'remove';
    var textColor = type.indexOf(uploadWarning) != -1 ? uploadWarning : uploadDanger;

    updateBadge(true, type);

    $(id).html(
        '<p class="text-' + textColor + '">'+
            '<span class="glyphicon glyphicon-' + icon + '" aria-hidden="true"></span> '+
            errorMsg +
        '</p>'
    );

    $('#' + fileId + ' .progressAction').hide();
}

// shows the upload progress bar for a given file (by file ID)
function showProgressBarForFile(fileId, progressValue) {
    var id = '#' + fileId + ' .progress-bar.progress-bar-striped.active';
    var progress = progressValue + '%';

    $(id).css('width', progress);
    $(id).attr('aria-valuenow', progressValue);
    $(id).html(progress);
}

// transfer file to iRODS message
function showTransferFileIRODSMsg(fileId) {
   showTransferMsg(fileId, icon = 'glyphicon-hourglass', "Transferring file to IRODS...");
}

function showTransferCompletedMsg(fileId, msg) {
   showTransferMsg(fileId, icon = 'glyphicon-ok', msg);
}

function showTransferMsg(fileId, icon, msg) {
    var progressWrapper = '#' + fileId + ' .progressWrapper';
    var progressAction = '#' + fileId + ' .progressAction';
    var htmlMsg = '<p class="text-success">';
        htmlMsg +=  '<span class="glyphicon ' + icon + '" aria-hidden="true"></span> ';
        htmlMsg +=  msg;
        htmlMsg += '</p>';

    updateBadge();

    $(progressWrapper).html(htmlMsg);
    $(progressAction).hide();
}

function updateBadge(hasError, errorType) {
    var badge = $('#uploadStatusIcon .badge');
    var badgeColor = uploadSuccess;

    if (hasError) badgeColor = errorType.indexOf(uploadWarning) != -1 ? uploadWarning : uploadDanger;

    if(badgeColor == uploadWarning && badge.hasClass(uploadDanger)) return;
    if(badgeColor == uploadSuccess && (badge.hasClass(uploadWarning) || badge.hasClass(uploadDanger))) return;

    badge.removeClass(uploadDanger);
    badge.removeClass(uploadWarning);
    badge.removeClass(uploadSuccess);

    badge.addClass(badgeColor);
}

// updates page title with upload transfer status
function updatePageTitle(progressValue, fileName, percentSizeAllFiles) {
    $('title').html(progressValue + '% uploaded for ' + fileName + ' | ' + percentSizeAllFiles + '% in total');
}

/**
 * Function that resolves a file name in a list of files that will be sent to the Metalnx server.
 */
function resolveFileName(fileName) {
	
	if (resolvedFileNames.indexOf(fileName) < 0) {
		return fileName;
	}
	
	var i = 1, 
		newFileName, 
		basename = fileName.substr(0, fileName.lastIndexOf('.')),
		extension = fileName.substr(fileName.lastIndexOf('.'));
	
	do {
		newFileName = basename + " (" + i + ")" + extension;
		i++;
	} while (resolvedFileNames.indexOf(newFileName) >= 0);
	
	
	return newFileName;
}

/**
 * Function that asks the server upload information of a file to resume upload.
 */
function getWhereUploadStopped(index, fileName) {
	var url = "/emc-metalnx-web/upload/resume/";
	$('#'+index+' .paused').val("false");
	$('#'+index+' .progressAction .btn.btn-default.btn-xs').attr('onclick', 'pause('+index+', \''+fileName+'\');');
	$('#'+index+' .progressAction .btn.btn-default.btn-xs span.glyphicon').removeClass('glyphicon-play');
	$('#'+index+' .progressAction .btn.btn-default.btn-xs span.glyphicon').addClass('glyphicon-pause');
	ajaxEncapsulation(url, "POST", {fileName : fileName}, resumeUpload, null, 
			null, null); 
}

/**
 * Function that restarts a paused uploaded.
 * @param response
 */
function resumeUpload(response) {
	var jsonObj = jQuery.parseJSON(response);
	var resumeFileName = jsonObj.fileName;
	var resumeLastPartUploaded =  Number(jsonObj.lastPartUploaded) + 1;
	var resumeListOfChunksUploaded = jsonObj.listOfChunksUploaded;

	$.each(files, function(index, file){
		if(file.name == resumeFileName) {
			var url = "/emc-metalnx-web/upload/";
			var startByteForPart = 0;
			var endByteForPart = partSize;

			//last file part
			if(endByteForPart > file.size) {
				endByteForPart = file.size;	
			}
			chunksSent = 0;
			$('#'+index+' .paused').val("false");
			sendFilePart(files, index, resumeLastPartUploaded, index);	        			
		}
	});
}

function setOperationInProgress() {
	oldBreadCrumbContent = $(".breadcrumb").html();
	$(".breadcrumb a").attr('onclick', '');
	$(".breadcrumb a").css('color', '#ccc');
	$(".breadcrumb span").css('color', '#ccc');
	$(".breadcrumb a").css('cursor', 'default');
	operationInProgress = true;
}

function unsetOperationInProgress() {
	$(".breadcrumb").html(oldBreadCrumbContent);
	operationInProgress = false;
}

$(document).ready(function(){
	$('body').on('click', '#uploadStatusIcon li.dropdown a', function(e){
		$(this).parent().toggleClass('open');
		$(this).parent().find('li').removeClass('open');
	});
	$('body').on('click', function (e) {
		if (!$('#uploadStatusIcon li.dropdown').is(e.target) 
				&& $('#uploadStatusIcon li.dropdown').has(e.target).length === 0 
				&& $('.open').has(e.target).length === 0
				&& !$('.progressAction button.btn.btn-default.btn-xs').is(e.target) 
				&& $('.progressAction button.btn.btn-default.btn-xs').has(e.target) 
		) {
			$('#uploadStatusIcon li.dropdown').removeClass('open');
		}
	});
});


/*
 * JavaScript CRC-32 implementation
 */

function crc32_generate(polynomial) {
	var table = new Array();
	var i, j, n;

	for (i = 0; i < 256; i++) {
		n = i;
		for (j = 8; j > 0; j--) {
			if ((n & 1) == 1) {
				n = (n >>> 1) ^ polynomial;
			} else {
				n = n >>> 1;
			}
		}
		table[i] = n;
	}

	return table;
}

function crc32_initial() {
	return 0xFFFFFFFF;
}

function crc32_final(crc) {
	crc = ~crc;
	return crc < 0 ? 0xFFFFFFFF + crc + 1 : crc;
}

function crc32_compute_string(polynomial, str) {
	var crc = 0
	var table = crc32_generate(polynomial)
	var i

	crc = crc32_initial();

	for (i = 0; i < str.length; i++)
		crc = (crc >>> 8) ^ table[str.charCodeAt(i) ^ (crc & 0x000000FF)];

	crc = crc32_final(crc);
	return crc;
}

function crc32_compute_buffer(polynomial, data) {
	var crc = 0;
	var dataView = new DataView(data);
	var table = crc32_generate(polynomial);
	var i;

	crc = crc32_initial();

	for (i = 0; i < dataView.byteLength; i++)
		crc = (crc >>> 8) ^ table[dataView.getUint8(i) ^ (crc & 0x000000FF)];

	crc = crc32_final(crc);
	return crc;
}