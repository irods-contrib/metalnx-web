var editor;

$(document).ready(function(){
	var code = $(".cm-textarea")[0];
  	editor = CodeMirror.fromTextArea(code,{
  		theme: "default",
  		lineNumbers : true 		
  	});
  	
  	editor.getDoc().markText({
    	  line: 5,
    	  ch: 10
    	}, {
    	  line: 5,
    	  ch: 39
    	}, {
    	  css: "color : red"
    	});
    	
  	
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status, jqXHR){
		var dispData;		
		var contentType = jqXHR.getResponseHeader("content-type") || "";
		var mode; 
		
		if (contentType.indexOf('text') > -1) {		     
			dispData = data,
			mode = "plain/text";
		}		
		if (contentType.indexOf('xml') > -1) {		     
			dispData = xmlToString(data);
			mode = "xml";
		}
		if (contentType.indexOf('json') > -1) {
			dispData = JSON.stringify(data);
			mode = "application/ld+json";
		} 
		if (contentType.indexOf('html') > -1) {
			dispData = JSON.stringify(data);
			mode = "html"
			
		} 
		
		console.log("mode ::" +mode);
		editor.setOption("mode" , mode);
		editor.getDoc().setValue(dispData);
		
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
	toastr.success("Successfully Edited!!" , "success");
}

function cancel() {
	$.get("/emc-metalnx-web/preview/dataObjectPreview/", function(data, status){
		editor.getDoc().setValue(data);
    });	
}

function xmlToString(xmlData) { 

    var xmlString;
    //IE
    if (window.ActiveXObject){
        xmlString = xmlData.xml;
    }
    // code for Mozilla, Firefox, Opera, etc.
    else{
        xmlString = (new XMLSerializer()).serializeToString(xmlData);
    }
    return xmlString;
}   


