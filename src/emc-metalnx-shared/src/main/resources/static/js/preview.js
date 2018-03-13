var editor;

$(document).ready(function(){
	var code = $(".cm-textarea")[0];
  	editor = CodeMirror.fromTextArea(code,{
  		mode: "scheme",
  		lineNumbers : true
  		
  	});
  	
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status){
		editor.getDoc().setValue(data);
    });		
	
});

/*function looksLikeScheme(code) {
    return !/^\s*\(\s*function\b/.test(code) && /^\s*[;\(]/.test(code);
  }
function update() {
	alert("update");
    editor.setOption("mode", looksLikeScheme(editor.getValue()) ? "scheme" : "javascript");
}*/
  
function save() {
	var data =  editor.getValue();
	var url = "/emc-metalnx-web/preview/save/";
	ajaxEncapsulation(url, "POST", {data: data}, confirmSave, null, null, null);	
	
}

function confirmSave(data){
	//$('#successConfirmationModal').modal();	
	toastr.success("Successfully Edited!!" , "success")
}

function cancel() {
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status){
		editor.getDoc().setValue(data);
    });	
}