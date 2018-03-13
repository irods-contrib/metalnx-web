$(document).ready(function(){
	$('#csv').jexcel({
	    csv:'/emc-metalnx-web/preview/dataObjectPreview/',
	    csvHeaders:true,
	    colWidths: [70, 500,500],
	});
});

function save() {
	alert("this will save the data!!");
}
function cancel() {
	alert("this will cancel the event!!");
}