/*
 * Updates the results counter over the table.
 */
function updateCounting(searchInputId, tBodyId, countId, alertId, tableId, countDisplay, queryString) {
	var numOfVisibleRows = $("#" + tBodyId + " > tr:visible").length;

	if (numOfVisibleRows == 0) {
		if ($("#" + searchInputId).val() != "") {
			$("#" + queryString).html($("#" + searchInputId).val());
			$("#" + alertId).show();
			$("#" + tableId).hide();
			$("#" + countDisplay).hide();	
		}
		else {
			$("#" + countDisplay).show();	
			$("#" + tableId).hide();
			$("#" + alertId).hide();			
		}
	}
	else {
		$("#" + alertId).hide();
		$("#" + tableId).show();
		$("#" + countDisplay).show();
	}
	$("#" + countId).html(numOfVisibleRows);
}

/*
 * Sets the input text box as search box for the table.
 */
function filterTable(searchInputId, tBodyId, countId, alertId, tableId, countDisplay, queryString) {

	updateCounting(searchInputId, tBodyId, countId, alertId, tableId, countDisplay, queryString);

	$("#" + searchInputId).keyup(function(e) {
		
		var data = this.value.trim().split(" ");
						    
		var jo = $("#" + tBodyId).find("tr");

		if (this.value == "") {
			jo.show();
			updateCounting(searchInputId, tBodyId, countId, alertId, tableId, countDisplay, queryString);
			$("#" + alertId).hide();
			$("#" + tableId).show();
			return;
		}
						   
		jo.hide();
		
		jo.filter(function (i, v) {
			var $t = $(this);
			for (var d = 0; d < data.length; ++d) {
				if ($t.text().toLowerCase().indexOf(data[d].toLowerCase()) > -1) {
					return true;
				}
			}
			return false;
		}).show();
		
		$("#" + tableId).show();
		updateCounting(searchInputId, tBodyId, countId, alertId, tableId, countDisplay, queryString);
	});
}