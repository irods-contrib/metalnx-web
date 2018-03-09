var editor;
$(document).ready(function(){
	alert("On page load !!");
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status){
        $('.cm-textarea').val(data);
        var code = $(".cm-textarea")[0];
     	editor = CodeMirror.fromTextArea(code,{
     		mode: "scheme",
     		lineNumbers : true
     		
     	});
    });		 
});

function looksLikeScheme(code) {
    return !/^\s*\(\s*function\b/.test(code) && /^\s*[;\(]/.test(code);
  }
function update() {
	alert("update");
    editor.setOption("mode", looksLikeScheme(editor.getValue()) ? "scheme" : "javascript");
}
  
function save() {
	var data =  $('.cm-textarea').val();
	alert("data ::" +data)
	var url = "/emc-metalnx-web/preview/save/";
	ajaxEncapsulation(url, "POST", {data: data}, confirmSave, null, null, null);		
}

function confirmSave(data){
	//alert("success");
	//alert(data.success);
	//alert(data);
	alert("confirmed save !!");
}

function cancel() {
	alert("this will cancel the event!!");
}