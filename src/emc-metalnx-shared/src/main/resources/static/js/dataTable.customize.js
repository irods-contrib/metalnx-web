//patterns for dom option in datatables
var dtPatternForMetadata =  '<"row"<" pull-left col-sm-12 col-md-12">>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-6"l<"download_csv">><"col-md-6"i>>>'+
                            '<"row"<"col-md-12 minHeightForLoading"tr>>'+
                            '<"row"<"col-md-12"p>>';
var dtPatternMlxCollections =  '<"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-8 col-sm-8 col-xs-8"l<"toolbar">><"col-md-4 col-sm-4 col-xs-4"f>><"col-md-12 col-xs-12"<"msg">i>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"tr>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"p>>';
var dtPatternMlxStandard =  '<"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-4 col-xs-4"l<"toolbar">><"col-md-5 col-xs-4"><"col-md-3 col-xs-4"f>><"col-md-12 col-xs-12"i>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"tr>>'+
                            '<"row"<"col-md-12 col-lg-12 col-xs-12"p>>';
var dtPatternMlxTemplatesList = '<"row"<"col-md-12 col-lg-12 col-xs-12"<"col-md-6 col-xs-6"l<"toolbar">><"col-md-6 col-xs-6"f>><"col-md-12 col-xs-12"i>>'+
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
	    "processing":     '<img class="center-block" src="/emc-metalnx-web/images/table_loading.svg" />',
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


//Adds Action button on Collection table
function addCollectionActions(table_id, datatable,advancedView){
    console.log("## dataTable.customize.js advancedView:" + advancedView);
    var replicate = '';
    if(advancedView){
      replicate = replicate +
                    '<li>'+
                      '<a href="#" id="replicateBtn" class="hideElement" data-toggle="modal" data-target="#replicateModal"><span><i class="fa fa-clone"></i></span> Replicate</a>'+
                    '</li>';
    }
    else if(!advancedView){
      repilcate = '';
    }
    $("div.toolbar").html(
        '<div id="actions" class="btn-group pull-left">'+
        '   <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" disabled="">'+
        '       <span>Action</span> &nbsp; <span class="caret"></span> <span class="sr-only">Toggle Dropdown</span> '+
        '   </button> '+
        '   <ul class="dropdown-menu" role="menu"> '+
        '       <li>'+
        '           <a href="#" id="moveBtn" class="hideElement" data-toggle="modal" data-target="#moveModal"><span><i class="fa fa-exchange"></i> </span><span>Move</span></a>'+
        '       </li>'+
        '       <li>'+
        '           <a href="#" id="copyBtn" class="hideElement" data-toggle="modal" data-target="#copyModal"><span><i class="fa fa-files-o"></i></span> <span>Copy</span></a>'+
        '       </li>'+
                replicate +
        '       <li>'+
        '           <a href="#" id="modifyBtn" class="hideElement" onclick="showModifyForm();"><span><i class="fa fa-pencil-square-o"> </i></span> <span>Edit</span></a>'+
        '       </li><li><a href="#" id="applyTemplatesBtn" class="hideElement" onclick="listAllTemplates();"><span><i class="fa fa-th"></i></span> <span>Apply Template</span></a>'+
        '       </li>'+
        '       <li><a href="#" id="createTicketBtn" class="hideElement" onclick="getTicketCreationForm();"><span><i class="fa fa-list-alt"> </i></span> <span>Create Ticket</span></a></li>'+
        '       <li> <a id="downloadBtn" href="/emc-metalnx-web/fileOperation/prepareFilesForDownload/" class="hideElement"><span><i class="fa fa-cloud-download"></i></span> <span>Download</span> </a>'+
        '       </li>'+
        '       <li class="divider"></li><li><a href="#" id="deleteBtn" class="hideElement" data-toggle="modal" data-target="#deleteModal"><span><i class="fa fa-trash-o"></i> </span> <span>Delete</span></a>'+
        '       </li>'+
        '   </ul>'+
        '</div>'  +
        '<div id="actionsWait" class="col-xs-12 hideElement">' +
        '   <div id="panelWait">' +
        '       <div class="progress">'+
        '           <div class="progress-bar progress-bar-striped active" '+
        '                   role="progressbar" aria-valuenow="100" aria-valuemin="0" '+
        '                    aria-valuemax="100" style="width: 100%"><span id="actionLabel"></span>'+
        '           </div>' +
        '       </div>'+
        '   </div>'+
        '</div>'
    );

	$("#"+table_id+"_length").addClass("pull-right");
}

//Adds delete all tickets button on tickets table
function addDeleteTickets(){
    $("div.toolbar").html(
        '   <button id="showDeleteTicketsModalBtn" type="button" class="btn btn-default" disabled="true" onclick="showDeleteTicketsModal();">' +
        '       Delete' +
        '   </button> '
    );
}

//Adds Action button on Collection table
function addCollectionMetadataDelBtn(table_id, datatable){
    $("div.toolbar").html(
        '<button' +
        '   id="delMetadataBtn" class="btn btn-default btn-property" href="#">' +
        '        <i class="fa fa-trash-o"></i> <span id="delMetadataBtnLabel"></span>' +
        '</button>'
    );
}

//Adds Action button on Template table
function addTemplateActionBtn(table_id, datatable){
    $('div.toolbar').html(
        '<div id="actions" class="pull-left"><div class="btn-group">'+
            '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" disabled="">' +
                '<span>Action</span> &nbsp;' +
                '<span class="caret"></span>' +
                '<span class="sr-only">Toggle Dropdown</span>' +
            '</button>' +
           '  <ul class="dropdown-menu" role="menu">' +
                '<li>'+
                    '<a href="#" onclick="javascript:exportFilesToXML();" class=""><i class="fa fa-external-link"></i> Export</a>' +
                '</li>' +
                '<li>' +
'                    <a href="#" id="removeTemplatesBtn" onclick="javascript:confirmTemplateRemoval();" class=""><i class="glyphicon glyphicon-trash"></i> Delete</a>' +
                '</li>' +
            '</ul>' +
        '</div></div>'
    );
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

$.fn.dataTable.ext.errMode = function(xhr, textStatus) {
    location.reload();
};
