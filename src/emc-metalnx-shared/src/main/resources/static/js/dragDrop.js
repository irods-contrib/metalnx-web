/* This is code that will allow the dragging and dropping of files to upload */

var isAdvancedUpload = function() {
   var div = document.createElement('div');
   return (('draggable' in div) || ('ondragstart' in div && 'ondrop' in div)) && 'FormData' in window && 'FileReader' in window;
}();

var $form = $('#uploadModal');
var $well = $('#dropArea');

if (isAdvancedUpload) {
  $form.addClass('has-advanced-upload');

	var droppedFiles = false;
	$form.on('drag dragstart dragend dragover dragenter dragleave drop', function(e) {
	  e.preventDefault();
    e.stopPropagation();
	})
  .on('drop', function(e) {
		droppedFiles = e.originalEvent.dataTransfer.files;
    $('input[type="file"]').prop('files', droppedFiles);
	});

	$well.on('dragover dragenter', function() {
    $well.addClass('is-dragover');
	})
	.on('dragleave dragend drop', function() {
		$well.removeClass('is-dragover');
	});
}
