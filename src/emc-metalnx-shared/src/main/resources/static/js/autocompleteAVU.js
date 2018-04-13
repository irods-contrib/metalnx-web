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
	var attrVal;
	var attributes;
	var attr;
	
	var nameArray = [];
	
	/*var attributes = $.get("/emc-metalnx-web/avuautocomplete/getMetadataAttrMock", function(data, status, jqXHR){
		alert("getting jason data");		
		console.log("data :: " +data);
		return data;
	});*/
	

	var options = {
	    source: function( request, response ) {
	        $.ajax({
	            dataType: "json",
	            type : 'Get',
	            url: '/emc-metalnx-web/avuautocomplete/getMetadataAttr',
	            success: function(data) {
	                $('input.metadataAttr').removeClass('ui-autocomplete-loading');  
	                // hide loading image

	                response( $.map( data, function(item) {
	                	console.log(item.cityName);
	                   return {
	                	   label : item.cityName,
	                	   value : item.id
	                   };
	                   
	                }));
	            },
	            error: function(data) {
	                $('input.metadataAttr').removeClass('ui-autocomplete-loading');  
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
	    }
	
	};
	
	var selector = 'input.metadataAttr';
	$(document).on('keydown.autocomplete', selector, function() {
		$(this).autocomplete(options);
		
	});
	
	
	/*
	console.log(attributes);
	
	var options = {
			minLength: 2,
			autoFocus: true,	       	                                                      
			source: function(request, response) {
				var _regexp = new RegExp(request.term, 'i');
				var data = attributes.filter(function(item) {
					return item.cityName.match(_regexp);
				});
				response(data);
			},
			
	};
*/
	
	
 /* $('.metadataAttr').autocomplete({
	  source : availableTutorials
  });*/

/*  $('.metadataAttr').each(function() {

	 
    var _this = $(this);
    var _data = _this.data();

    _this.autocomplete({
    	minLength: 2,
        autoFocus: true,

        source: function(request, response) {
          var _regexp = new RegExp(request.term, 'i');
          var data = attributes.filter(function(item) {
            return item.cityName.match(_regexp);
          });
          response(data);
        },

        search: function() {       
        },

        response: function() {     
        },

        focus: function(event, ui) {
          _this.val(ui.item[_data.item_label]);
          event.preventDefault();
        },

        select: function(event, ui) {
          _this.val(ui.item[_data.item_label]);
         var attr = ui.item[_data.item_label];
         if(attr == "Rome"){
        	 attrVal=[100,200,300];
         }
         else if(attr == "Tokyo"){
        	 attrVal=[1000,2000,3000];
         }

         console.log("Selected attributes value :: " +attrVal);
         var $metadataValue = $("#metadataValue0");
         $metadataValue.empty();
 		 $.each(attrVal, function(index, value) {
 			$metadataValue.append("<option>" + value + "</option>");
 		});

          event.preventDefault();
        }
      })
      .data('ui-autocomplete')._renderItem = function(ul, item) {
    	console.log(item[_data.item_label]);
        return $('<li></li>')
          .data("item.autocomplete", item)
          .append('<a>' + item[_data.item_label] + '</a>')
          .appendTo(ul);
      };
    // end autocomplete
  });*/
});