//patterns for dom option in datatables
var dtPatternForMetadata = '<"row"<"download_csv pull-left col-sm-12 col-md-12">><"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-6"l<"toolbar">><"col-md-6"i>>><"row"<"col-md-12"tr>><"row"<"col-md-12"p>>';
var dtPatternMlxStandard =  '<"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-3 col-xs-4"l<"toolbar">><"col-md-6 col-xs-4"p><"col-md-3 col-xs-4"f>><"col-md-12 col-xs-12"i>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"tr>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"p>>';
var dtPatternMetadataTemplate = '<"row"<"col-sm-12 col-md-12"<"col-xs-6 #info_templateFieldsListTable"i><"col-xs-6"f>>><"row"<"col-sm-12 col-md-12"tr>>';
var dtPatternUserMgmt = '<"row"<"col-sm-12 col-md-12"<"col-xs-4"l><"col-xs-4"i><"col-xs-4"f>>><"row"<"col-sm-12 col-md-12"tr>><"row"<"col-md-12"p>>';

//hash containing messages to be translated
var i18n = {
	    "decimal":        "",
	    "emptyTable":     "No data available in table",
	    "info":           "Showing _START_ to _END_ of _TOTAL_ entries",
	    "infoEmpty":      "Showing 0 to 0 of 0 entries",
	    "infoFiltered":   "(filtered from _MAX_ total entries)",
	    "infoPostFix":    "",
	    "thousands":      ",",
	    "lengthMenu":     "_MENU_",
	    "loadingRecords": "Loading...",
	    "processing":     '<img class="center-block" src="../images/ajax_loader.gif" /><p class="text-center" >Loading...</p>',
	    "search":         "_INPUT_",
	    "searchPlaceholder": "Search...",
	    "zeroRecords":    "No matching records found",
	    "paginate": {
	        "first":      "First",
	        "last":       "Last",
	        "next":       '<i class="fa fa-chevron-right"></i>',
	        "previous":   '<i class="fa fa-chevron-left"></i>'
	    },
	    "aria": {
	        "sortAscending":  ": activate to sort column ascending",
	        "sortDescending": ": activate to sort column descending"
	    }
	}

//Adds GoToPage button on datatables
function addGoToPage(table_id, datatable){
	
	// Adding a content to the div toolbar:
		//GotoPage feature
	$("div.toolbar").html(
		'<div class="input-group input-group-sm">'+
			'<input type="text" class="form-control" id="goToPageInput" placeholder="Page #"/>'+
			'<div class="input-group-btn">'+
				'<button id="goToPageBtn" class="btn btn-default"><span class="glyphicon glyphicon-chevron-right"></span></button>'+
			'</div>'+
		'</div>'
	);
	
	$("#"+table_id+"_length").addClass("pull-left");
	
	//jquery functions bound to the html code added above
	$('#goToPageBtn').on( 'click', function () {
		var pageNum = $("#goToPageInput").val();        	
		pageNum = parseInt(pageNum);
		
		if(isNaN(pageNum)){
	   		$("#goToPageInput").val("");
			return;
		}
	   	
	   	if(pageNum > datatable.page.info().pages || 1 > pageNum) {
	   		$("#goToPageInput").val("");
	   		return;
	   	}  
	    datatable.page( pageNum-1 ).draw( 'page' );
	});
	$("#goToPageInput").keypress(function(e){
		if(e.which == 13) {
			$("#goToPageBtn").click();
	    }
	});
}

/**
 * Resets the start page of a table.
 * After any file operation (copy, move, delete, etc), the table needs to go back to the first page.
 * */
function resetDataTablesStart () {
	for(var i = 0; i < localStorage.length; i++) {
		var key = localStorage.key(i);
		if(key.indexOf('emc-metalnx-web') > -1) {
			var data = JSON.parse(localStorage.getItem(key));
			data.start = 0;
			localStorage.setItem(key, JSON.stringify(data));
		}
	}
}