$(document).ready(function(){
	$('#csv').jexcel({
	    csv:'/emc-metalnx-web/preview/dataObjectPreview/',
	    csvHeaders:true,
	    colWidths: [70, 500,500],
	});
});

function save() {
	var data = $("#csv").jexcel("getData");
	alert("this will save the data!!" +data+ " , ends !!");
	var url = "/emc-metalnx-web/preview/save/";
	ajaxEncapsulation(url, "POST", {data: data}, confirmSave, null, null, null);	
}

function confirmSave(data){
	alert("success");
	toastr.success("Successfully Edited!!" , "success")
}

function cancel() {
	$('#csv').jexcel({
	    csv:'/emc-metalnx-web/preview/dataObjectPreview/',
	    csvHeaders:true,
	    colWidths: [70, 500,500],
	});
}