$(document).ready(function() {
	function getNotification() {
		$.get( "/emc-metalnx-web/notification/unseen/", function( data ) {
			$( ".result" ).html( data );
			console.log(data);	
			$('#noti_Counter').css({
				opacity : 0
			}).text(data) // ADD DYNAMIC VALUE (YOU CAN EXTRACT DATA FROM DATABASE OR XML).
			.css({
				top : '-10px'
			}).animate({
				top : '-2px',
				opacity : 1
			}, 500);

		});
	}
	window.onload = function () {
		getNotification(); //Make sure the function fires as soon as the page is loaded
		setTimeout(getNotification, 60000); //Then set it to run again after ten minutes
	}


	$('#noti_Button').click(
			function() {
				alert("notification button clicked");
				location.href = "/emc-metalnx-web/notification/";
				$('#noti_Counter').fadeOut(
				'slow'); // HIDE THE COUNTER.

				return false;
			});

	/*Notification ends */

	alert("notification");
	/*  $('#noti_Counter')
			 .css({ opacity: 0 })
			 .text('7')      // ADD DYNAMIC VALUE (YOU CAN EXTRACT DATA FROM DATABASE OR XML).
			 .css({ top: '-10px' })
			 .animate({ top: '-2px', opacity: 1 }, 500); */

	/*  $('#noti_Button').click(function () {
				 alert("notification button clicked");
			     $('#noti_Counter').fadeOut('slow');     // HIDE THE COUNTER.

			     return false;
			 }); */

	$('.deleteNotification').click(function(){			
		alert("deleting notification ");
		$('#deleteNotifications').modal('show');
	});
	$('input.notificationCheckbox').click(function(){
		if($('input.notificationCheckbox').length == $('input.notificationCheckbox:checked').length){
			alert("All checked");
			// $('#checkAllMetadata').prop('checked', true);
			//  $('#delMetadataBtn').prop("disabled", false);
		}else{
			if($('input.notificationCheckbox:checked').length == 0){
				alert("None checked");
				// $('#delMetadataBtn').prop("disabled", true);
			}else{
				alert("Some checked");
				// $('#delMetadataBtn').prop("disabled", false);
				//  $('#delMetadataBtn').removeClass("disabled");
			}
			//$('#checkAllMetadata').prop('checked', false);
		}
	});

});
/*function getAllNotifications(path){

	console.log("getInfoDetails()");
	//window.location.hash = "info";
	var url = "/emc-metalnx-web/notification/";
	ajaxEncapsulation(url, "POST", {path: path}, displayInfoDetails, null, null, null);
}*/
function deleteNotificationList(){
	alert("delete notifications confirmed !!");
	//var currentPath = [[${currentPath}]];
	var params = [];

	//if($('#deleteOneAVU').val() != "true"){
		$('.notificationCheckbox:checked').each(function(){
			params.push({ "uuid": $(this).attr('data-val')});
			console.log("uuid :: " +$(this).attr('data-val'));
		});
	/*}else{
		params.push({"attribute": $('#deleteMetadataAttribute').val(), "value":  $('#deleteMetadataValue').val(), "unit": $('#deleteMetadataUnit').val()});
	}*/
	/*$('#table-loader').show();
	$("#table-loader").nextAll().remove();
	$('#deleteMetadataModal').modal('hide');
	$('.modal-backdrop.fade.in').remove();*/
	ajaxEncapsulation("/emc-metalnx-web/notification/deleteNotifications", "POST", {params: params, length: params.length}, displayNotification, null, null, null);
}
function markToSeen(){
	alert("delete notifications confirmed !!");
	//var currentPath = [[${currentPath}]];
	var params = [];

	//if($('#deleteOneAVU').val() != "true"){
		$('.notificationCheckbox:checked').each(function(){
			params.push({ "uuid": $(this).attr('data-val')});
			console.log("uuid :: " +$(this).attr('data-val'));
		});
	/*}else{
		params.push({"attribute": $('#deleteMetadataAttribute').val(), "value":  $('#deleteMetadataValue').val(), "unit": $('#deleteMetadataUnit').val()});
	}*/
	/*$('#table-loader').show();
	$("#table-loader").nextAll().remove();
	$('#deleteMetadataModal').modal('hide');
	$('.modal-backdrop.fade.in').remove();*/
	ajaxEncapsulation("/emc-metalnx-web/notification/markToSeen", "POST", {params: params, length: params.length}, displayNotification, null, null, null);
}