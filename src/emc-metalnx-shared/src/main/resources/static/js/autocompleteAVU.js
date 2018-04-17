$.widget("ui.autocomplete", $.ui.autocomplete, {

	_renderMenu: function(ul, items) {
		var that = this;
		ul.attr("class", "nav nav-pills nav-stacked  bs-autocomplete-menu");
		$.each(items, function(index, item) {
			that._renderItemData(ul, item);
		});
	},

	_resizeMenu: function() {
		var ul = this.menu.element;
		ul.outerWidth(Math.min(
				// Firefox wraps long text (possibly a rounding bug)
				// so we add 1px to avoid the wrapping (#7513)
				ul.width("").outerWidth() + 1,
				this.element.outerWidth()
		));
	}

});

$(document).ready(function(){

	var options = {
			source: function( request, response ) {
				$.ajax({
					dataType: "json",
					type : 'Get',           
					url: '/emc-metalnx-web/avuAutoComplete/getMetadataAttrs',
					success: function(data) {
						//$('input.metadataAttr').removeClass('ui-autocomplete-loading');  
						// hide loading image	              
						var _regexp = new RegExp(request.term, 'i');
						var data = data.elements.filter(function(item) {
							return item.match(_regexp);
						});

						response(data);	              
					},
					error: function(data) {
						//  $('input.metadataAttr').removeClass('ui-autocomplete-loading');  
					}
				});
			},
			minLength: 3,
			open: function() {},
			close: function() {},
			focus: function(event,ui) {

				$(this).val(ui.item.label);
				event.preventDefault();
			},
			select: function(event, ui) {
				$(this).val(ui.item.label);
				event.preventDefault();
				var selectedObj = ui.item.label; 
				console.log("Value selected :: " + selectedObj);
				var _id = $(this).attr("id");
				getAttributeValues(selectedObj, _id);
			}

	};


	var selector = 'input.metadataAttr';
	$(document).on('keydown.autocomplete', selector, function() {
		$(this).autocomplete(options).data('ui-autocomplete')._renderItem = function(ul, item) {
			console.log(item.label);
			return $('<li></li>')
			.data("item.autocomplete", item)
			.append('<a>' + item.label + '</a>')
			.appendTo(ul);
		};
	});

});

function getAttributeValues(attributeName,id){

	var listId = $("#"+id).closest("div.metadataSearchRow").find("datalist").attr("id");	
	$.ajax({  	
		dataType: "json",
		type : 'Get',            
		url: '/emc-metalnx-web/avuAutoComplete/getMetadataValues',
		data: {attributeName : attributeName},
		success: function(data) {

			$(data.elements).each( function(index, item) {       		
				var option = $('<option value="'+item+'"></option>');
				$("#"+listId).append(option);
			});
		}

	});
}