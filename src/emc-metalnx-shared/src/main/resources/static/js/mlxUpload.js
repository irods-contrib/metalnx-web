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
var files;
var resolvedFileNames = [];

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

	$.each(files, function(index, file){
		var url = "/emc-metalnx-web/upload/uploadSimple/";
		var formData = new FormData();
        formData.append('file', file);
        formData.append('checksum', $('#inputChecksum').is(':checked'));
        formData.append('replica', $('#inputReplica').is(':checked'));
        formData.append('resources', $('#selectResource').val());
        formData.append('resourcesToUpload', $('#selectResourceToUpload').val());
        formData.append('overwriteDuplicateFiles', $('#inputOverwriteDuplicateFiles').is(':checked'));

		$('#uploadStatusIcon').removeClass('hide');
		$('#uploadStatusIcon ul.dropdown-menu').empty();

		$.ajax({
			url: url,
			type: "POST",
			data: formData,
			cache: false,
            contentType: false,
            processData: false,
			xhr: function() {
                var xhr = new window.XMLHttpRequest();
                //Upload progress
                xhr.upload.addEventListener("progress", function(evt){
                    if (evt.lengthComputable) {
                        var percentComplete = (evt.loaded / evt.total)*100;
                        var roundedPercent = Math.round(percentComplete);
                        if(percentComplete >= 100) {
                            $('#'+index+' .progressWrapper').html('<p style="color:green; text-align:center;"><span class="glyphicon glyphicon-hourglass" aria-hidden="true"> Transferring file to IRODS...</span></p>');
                        }else{
                            $('#'+index+' .progress-bar.progress-bar-striped.active').css('width', roundedPercent+'%');
                            $('#'+index+' .progress-bar.progress-bar-striped.active').attr('aria-valuenow', roundedPercent);
                            $('#'+index+' .progress-bar.progress-bar-striped.active').html(roundedPercent+'%');
                        }
                    }
                }, false);
                return xhr;
            },
			success: function () {
			    $('#'+index+' .progressWrapper').html('<p style="color:green; text-align:center;"><span class="glyphicon glyphicon-ok" aria-hidden="true"> Completed</span></p>');
			    getSubDirectories($('#navigationInput').val());
			    unsetOperationInProgress();
			},
		});

        uploadItems += '<li id="'+index+'"><a class="col-sm-12">'+
		'<input type="hidden" class="paused" value="false" />'+
		'<div class="col-sm-4" style="float:left; margin-right:10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">'+
		    '<span style="text-align:right" title="' + file.name + '">' + file.name + ' </span>'+
		'</div>'+
		'<div class="col-sm-7 progressWrapper">'+
            '<div class="progress" style="">'+
                '<div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">0%</div>'+
            '</div>'+
		'</div>'+
		'</a></li>';
	});

	$('#uploadStatusIcon ul.dropdown-menu').html(uploadItems);
});

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