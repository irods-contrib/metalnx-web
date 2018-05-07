$(document).ready(function(){
	
	$( "#datatype" ).change(function() {

		var valSelected = $( "#datatype option:selected" ).val();
		console.log( "value selected :: " +valSelected);
		var dataListflag = false ;

		if(!dataListflag){
			$(".dataList").hide();
		}
		

		if (valSelected == "date"){			  
			$("#defaultVal")
		    .replaceWith('<input type="text" class="form-control datepicker" id="defaultVal" th:field="*{value}"/>');
		}else if (valSelected == "boolean"){
			$("#defaultVal")
		    .replaceWith('<input type="checkbox" class="form-check-input" th:field="*{value}" id="defaultVal"/>');
		}else if (valSelected == "list"){
					
			$(".dataList").show();   			
			$("#defaultVal")
		    .replaceWith('<select id="defaultVal" class="form-control defaultValList " th:field="*{value}">' +
		          '<option value="1">1</option>' +
		          '<option value="2">2</option>' +
		          '<option value="3">3</option>' +
		          '<option value="4">4</option>' +
		          '<option value="5">5</option>' +
		        '</select>');						
		}else{
			$("#defaultVal")
		    .replaceWith('<input type="text" class="form-control" th:field="*{value}" maxlength="100" id="defaultVal" />');
		}
	});
	
	$('#dataList').keydown(function (e){
	    if(e.keyCode == 13){
	    	var val = $("#dataList").val();
	    	console.log("Val :: " +val);
	    	$('.defaultValList').append( '<option value="'+val+'" selected="selected">'+val+'</option>' );
	       //Your other logic 
	    }
	});
	
	
	
	
	$(".datepicker").datepicker();
});