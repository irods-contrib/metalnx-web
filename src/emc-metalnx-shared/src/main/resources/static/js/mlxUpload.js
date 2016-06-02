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
		var url = "/emc-metalnx-web/fileOperation/prepareFilesForUpload/";
		
		sizeAllFiles += fileSize;
		
		$('#uploadStatusIcon').removeClass('hide');
		$('#uploadStatusIcon ul.dropdown-menu').empty();

		ajaxEncapsulation(
			url, 
			"POST", 
			{
				fileName : fileName, 
				fileSize : fileSize,
				partSize : partSize, 
				totalParts : totalParts, 
				chunkSize : chunkSize,
				totalChunksPerPart : totalChunksPerPart, 
				totalChunks: totalChunks,
				checksum : $('#inputChecksum').is(':checked'),
				replica : $('#inputReplica').is(':checked'),
				resources : $('#selectResource').val(), 
				resourcesToUpload : $('#selectResourceToUpload').val(),
				overwriteDuplicateFiles : $('#inputOverwriteDuplicateFiles').is(':checked')
			},
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
				url:'/emc-metalnx-web/fileOperation/upload/',
				type:'POST',
				data: formData,
				cache: false,
				contentType: false,
				processData: false,
				success:  function(data) {
					//checking if chunk that was sent is corrupted
					if(data.indexOf('nullpointer') >= 0 || data.indexOf('corruptedfile') >= 0 || data.indexOf('transfererror') >= 0){
						if(data.indexOf('corruptedfile') >= 0){
							$('#'+index+' .progressWrapper').html(
								'<p style="color:red; text-align:center; font-size:12px;">'+
								'<span class="glyphicon glyphicon-remove" aria-hidden="true"></span> '+
								'Failed: File got corrupted.'+
								'</p>'
							);
						}
						else if(data.indexOf('nullpointer') >= 0) {
							$('#'+index+' .progressWrapper').html(
								'<p style="color:red; text-align:center; font-size:12px;">'+
								'<span class="glyphicon glyphicon-remove" aria-hidden="true"></span> '+
								'Failed: An error ocurred at the server.'+
								'</p>'
							);
						}
						else if(data.indexOf('transfererror') >= 0) {
							$('#'+index+' .progressWrapper').html(
								'<p style="color:red; text-align:center; font-size:12px;">'+
								'<span class="glyphicon glyphicon-remove" aria-hidden="true"></span> '+
								'Failed: Not enough free space in IRODS.'+
								'</p>'
							);
						}
						
						$('#'+index+' .progressAction').hide();
						//sending the next files
						if((currFilePos+1) < files.length) {
							chunksSent = 0;
							currFilePos++;
							sendFilePart(files, ++index, 0, currFilePos);
						}
						else if((currFilePos+1) == files.length) {
							getSubDirectories(data.substring(0, data.indexOf(':')));
							unsetOperationInProgress();
						}
					}
					else {
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

						$('#'+index+' .progress-bar.progress-bar-striped.active').css('width', progressValue+'%');
						$('#'+index+' .progress-bar.progress-bar-striped.active').attr('aria-valuenow', progressValue);
						$('#'+index+' .progress-bar.progress-bar-striped.active').html(progressValue+'%');

						$('title').html(progressValue + '% uploaded for ' + fileName + ' | ' + Math.round(percentSizeAllFiles) + '% in total');

						if(progressValue >= 99) {
							$('#'+index+' .progressWrapper').html('<p style="color:green; text-align:center;"><span class="glyphicon glyphicon-hourglass" aria-hidden="true"> Transferring file to IRODS...</span></p>');
							$('#'+index+' .progressAction').hide();
						}

						var totalChunksPerParts;
						if(endByteForPart > file.size) {
							endByteForPart = file.size;	
							totalChunksPerParts = Math.ceil((file.size % partSize)/chunkSize);
						}
						else {
							totalChunksPerParts = Math.ceil(filePart.size/chunkSize);
						}

						//sending the next parts
						if(file.size > partSize && chunksSent >= totalChunksPerParts && (currPart+1) < totalParts && ($('#'+index+' .paused').val().indexOf('false') >= 0)) {
							chunksSent = 0;
							sendFilePart(files, index, ++currPart, currFilePos);
						}    

						//sending the next files
						if(currPart == totalParts - 1 && (currFilePos+1) < files.length && chunksSent >= totalChunksPerParts) {
							$('#'+index+' .progressWrapper').html('<p style="color:green; text-align:center;"><span class="glyphicon glyphicon-ok" aria-hidden="true"> Completed</span></p>');
							$('#'+index+' .progressAction').hide();
							chunksSent = 0;
							currFilePos++;
							sendFilePart(files, ++index, 0, currFilePos);
						}else if(currPart == totalParts - 1 && (currFilePos+1) == files.length && chunksSent >= totalChunksPerParts){
							$('#'+index+' .progressWrapper').html('<p style="color:green; text-align:center;"><span class="glyphicon glyphicon-ok" aria-hidden="true"> Completed</span></p>');
							$('#'+index+' .progressAction').hide();
							getSubDirectories(data);
							unsetOperationInProgress();
							$('title').html(originalPagetitle);
						}
					}
				},
				error: function(data){
					var errorHtml = '<p style="color:red; text-align:center;">';
						errorHtml += 	'<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>';
						errorHtml += 	'Upload failed. Please, check if all resources are available.';
						errorHtml += '</p>';
						
					$('#'+index+' .progressWrapper').html(errorHtml);
					$('#'+index+' .progressAction').hide();
					unsetOperationInProgress();
					$('#uploadFailMessage').show();
					$('#uploadIcon').show();
					$('#showCollectionFormBtn').show();
					resolvedFileNames = [];
				},
				statusCode: {
					500: function(response){
						$('#container-fluid').html(response);
					},
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
	var url = "/emc-metalnx-web/fileOperation/resumeUpload/";
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
			var url = "/emc-metalnx-web/fileOperation/upload/";
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