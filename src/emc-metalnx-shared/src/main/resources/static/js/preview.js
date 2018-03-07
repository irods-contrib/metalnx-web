$(document).ready(function(){
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status){
        $('.cm-textarea').val(data);
        var code = $(".cm-textarea")[0];
     	var editor = CodeMirror.fromTextArea(code,{
     		lineNumbers : true,
     	});
    });		 
});

function save() {
	alert("this will save the data!!");
}
function cancel() {
	alert("this will cancel the event!!");
}