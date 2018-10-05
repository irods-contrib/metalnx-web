$(document).ready(function(){
	$('#csv').jexcel({
	    csv:'/emc-metalnx-web/preview/dataObjectPreview/',
	    csvHeaders:true,
	    colWidths: [70, 500,500],
	});
});

function save() {
	var data = $("#csv").jexcel("getData");
	var csvData = $.csv.fromArrays(data);
	console.log("Data :: "+data);
	console.log("csvData :: "+csvData);
	var url = "/emc-metalnx-web/preview/saveCsv/";
	ajaxEncapsulation(url, "POST", {data: csvData}, confirmSave, failSave, null, "text/csv", null);	
	//ajaxEncapsulation(url, "POST", {data: data}, confirmSave, failSave, null, 'text/csv;charset=utf-8' , null);	
}

function confirmSave(data){
	alert("success");
	toastr.success("Successfully Edited!!" , "success")
}

function failSave(){
	toastr.error("Something went wrong.Your chnages are not saved!!" , "Error")
}

function cancel() {
	$('#csv').jexcel({
	    csv:'/emc-metalnx-web/preview/dataObjectPreview/',
	    csvHeaders:true,
	    colWidths: [70, 500,500],
	});
}