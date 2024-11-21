
/*
 * Validates if the template form can  be submitted
 */
function templateFormCanBeSubmitted(){

	if($("#templateName").parent().hasClass("has-feedback has-error")){
		return false;
	}

	return true;
}

/*
 * Validates add/modify template form
 */
function templateFormValidator(urlValidateTemplateName) {
	$("#templateName").focusout(function() {
		validateCollectionNameAjax(urlValidateTemplateName);
	});
}

/*
 * Validates a template name using AJAX
 *  */
function validateTemplateNameAjax(urlValidateTemplateName) {
	var templateName = $.trim($("#templateName").val());
	var url = urlValidateTemplateName + templateName + "/";

	$("#invalidTemplateNameMsg").hide();
	$("#invalidTemplateNameIcon").hide();
	$("#emptyTemplateNameMsg").hide();

	if(templateName != ""){
		$.get(url, function(data) {
			//invalid template name
			if(data == "false") {
				$("#templateName").parent().addClass("has-feedback has-error");
				$("#invalidTemplateNameMsg").show();
				$("#invalidTemplateNameIcon").show();
				return false;
			}
			else {
				$("#templateName").parent().removeClass("has-feedback has-error");
				$("#invalidTemplateNameMsg").hide();
				$("#invalidTemplateNameIcon").hide();
				$("#emptyTemplateNameMsg").hide();
			}
		});
	}
	//blank group name
	else {
		$("#templateName").parent().addClass("has-feedback has-error");
		$("#emptyTemplateNameMsg").show();
		$("#invalidTemplateNameIcon").show();
		return false;
	}

	return true;
}

/*
 * Validates a collection name using AJAX
 */
function validateCollectionNameAjax(urlValidateCollectionName, callBackFunction){
	var collectionName = $.trim($("#inputCollectionName").val());
	var url = urlValidateCollectionName + "/";
	var rc = false;

	$("#invalidCollectionNameMsg").hide();
	$("#invalidCollectionNameIcon").hide();
	$("#emptyCollectionNameMsg").hide();

	if(collectionName != ""){
		$.ajax({
			url: url,
			complete: function(data) {
				if(data.responseText == "true") {
					$("#inputCollectionName").parent().removeClass("has-feedback has-error");
					$("#invalidCollectionNameMsg").hide();
					$("#invalidCollectionNameIcon").hide();
					$("#emptyCollectionNameMsg").hide();
					if(callBackFunction != null && callBackFunction != undefined) {
						rc = true;
						callBackFunction();
					}
				}
				else {
					$("#inputCollectionName").parent().addClass("has-feedback has-error");
					$("#invalidCollectionNameMsg").show();
					$("#invalidCollectionNameIcon").show();
				}
			},
			async: false
		});
	}
	else {
		$("#inputCollectionName").parent().addClass("has-feedback has-error");
		$("#emptyCollectionNameMsg").show();
		$("#invalidCollectionNameIcon").show();
	}

	return rc;
}

/*
 * Validates add/modify collection form
 */
function collectionFormValidator(urlValidateCollectionName) {
	$("#inputCollectionName").focusout(function() {
		validateCollectionNameAjax(urlValidateCollectionName);
	});
}

/*
 * Validates a resource name using AJAX
 */
function validateResourceNameAjax(urlValidateResourceName){
    var rc = true;
	var resourceName = $.trim($("#inputResourceName").val());
	var url = urlValidateResourceName + resourceName + "/";

	$("#invalidResourceNameMsg").hide();
	$("#invalidResourceNameIcon").hide();
	$("#emptyResourceNameMsg").hide();

	if(resourceName != ""){
		$.ajax({
		        url: url,
		        complete: function(data) {
            		//invalid Group name
            		if(data.responseText == "false") {
            			$("#inputResourceName").parent().addClass("has-feedback has-error");
            			$("#invalidResourceNameMsg").show();
            			$("#invalidResourceNameIcon").show();
            			rc = false;
            		}
            		else {
            			$("#inputResourceName").parent().removeClass("has-feedback has-error");
            			$("#invalidResourceNameMsg").hide();
            			$("#invalidResourceNameIcon").hide();
            			$("#emptyResourceNameMsg").hide();
            		}
            	},
            	async: false
		});
	}
	//blank group name
	else {
		$("#inputResourceName").parent().addClass("has-feedback has-error");
		$("#emptyResourceNameMsg").show();
		$("#invalidResourceNameIcon").show();
		rc = false;
	}
	return rc;
}

/*
 * Validates a host name entered for a resource
 */
function validateResourceHost() {
	//if the resource path input is not shown, we don't need to validate it
	if(!$("#inputResourceHost").is(':visible')){
		return true;
	}

	$("#inputResourceHost").parent().removeClass("has-feedback has-error");
	$("#emptyResourceHostMsg").hide();
	$("#invalidResourceHostIcon").hide();

	if($("#inputResourceHost").val().trim() == ""){
		$("#inputResourceHost").parent().addClass("has-feedback has-error");
		$("#emptyResourceHostMsg").show();
		$("#invalidResourceHostIcon").show();
		return false;
	}

	return true;
}

/*
 * Validates a path entered for a resource
 */
function validateResourcePath() {
	//if the resource path input is not shown, we don't need to validate it
	if(!$("#inputResourcePath").is(':visible')){
		return true;
	}

	$("#inputResourcePath").parent().removeClass("has-feedback has-error");
	$("#emptyResourcePathMsg").hide();
	$("#invalidResourcePathIcon").hide();

	if($("#inputResourcePath").val().trim() == ""){
		$("#inputResourcePath").parent().addClass("has-feedback has-error");
		$("#emptyResourcePathMsg").show();
		$("#invalidResourcePathIcon").show();
		return false;
	}

	return true;
}

/*
 * Validates a host name entered for an isilon resource
 */
function validateIsilonResourceHost() {
	//if the isilon resource host input is not shown, we don't need to validate it
	if(!$("#inputIsilonResourceHost").is(':visible')){
		return true;
	}

	$("#inputIsilonResourceHost").parent().removeClass("has-feedback has-error");
	$("#emptyIsilonResourceHostMsg").hide();
	$("#invalidIsilonResourceHostIcon").hide();

	if($("#inputIsilonResourceHost").val().trim() == ""){
		$("#inputIsilonResourceHost").parent().addClass("has-feedback has-error");
		$("#emptyIsilonResourceHostMsg").show();
		$("#invalidIsilonResourceHostIcon").show();
		return false;
	}

	return true;

}

/*
 * Validates a user name entered for an isilon resource
 */
function validateIsilonResourceUser() {

	//if the isilon user input is not shown, we don't need to validate it
	if(!$("#inputIsilonResourceUser").is(':visible')){
		return true;
	}

	$("#inputIsilonResourceUser").parent().removeClass("has-feedback has-error");
	$("#emptyIsilonResourceUserMsg").hide();
	$("#invalidIsilonResourceUserIcon").hide();

	if($("#inputIsilonResourceUser").val().trim() == ""){
		$("#inputIsilonResourceUser").parent().addClass("has-feedback has-error");
		$("#emptyIsilonResourceUserMsg").show();
		$("#invalidIsilonResourceUserIcon").show();
		return false;
	}

	return true;
}

/*
 * Validates the port entered for an isilon resource
 */
function validateIsilonResourcePort() {

	//if the isilon port input is not shown, we don't need to validate it
	if(!$("#inputIsilonResourcePort").is(':visible')){
		return true;
	}

	$("#inputIsilonResourcePort").parent().removeClass("has-feedback has-error");
	$("#emptyIsilonResourcePortMsg").hide();
	$("#invalidIsilonResourcePortIcon").hide();

	if($("#inputIsilonResourcePort").val().trim() == ""){
		$("#inputIsilonResourcePort").parent().addClass("has-feedback has-error");
		$("#emptyIsilonResourcePortMsg").show();
		$("#invalidIsilonResourcePortIcon").show();
		return false;
	}

	return true;
}

/*
 * Validates add/modify resource form
 */
function resourceFormValidator(urlValidateResourceName) {

    $("#inputResourceName").focusout(function() {
		validateResourceNameAjax(urlValidateResourceName);
	});

	$("#inputResourceHost").focusout(function() {
		validateResourceHost();
	});

	$("#inputResourcePath").focusout(function() {
		validateResourcePath();
	});

	$("#inputIsilonResourceHost").focusout(function() {
		validateIsilonResourceHost();
	});

	$("#inputIsilonResourcePort").focusout(function() {
		validateIsilonResourcePort();
	});

	$("#inputIsilonResourceUser").focusout(function() {
		validateIsilonResourceUser();
	});
}

/*
 * Validates if the resource form can  be submitted
 */
function resourceFormCanBeSubmitted(urlValidateResourceName, isAddAction){

	var isValidResourceName = validateResourceNameAjax(urlValidateResourceName);
	var isValidResourceHost = validateResourceHost();
	var isValidResourcePath = validateResourcePath();
	var isValidIsilonResourceHost = validateIsilonResourceHost();
	var isValidIsilonResourcePort = validateIsilonResourcePort();
	var isValidIsilonResourceUser = validateIsilonResourceUser();

	if(isValidResourceName && isValidResourceHost && isValidResourcePath && isValidIsilonResourceHost
		&& isValidIsilonResourcePort && isValidIsilonResourceUser) {
		return true;
	}

	return false;
}


/*
 * Validates a group using AJAX
 */
function validateGroupnameAjax(urlValidateGroupname){

	var groupname = $.trim($("#inputGroupname").val());
	var url = urlValidateGroupname + groupname + "/";

	$("#invalidGroupnameMsg").hide();
	$("#invalidGroupnameIcon").hide();
	$("#emptyGroupnameMsg").hide();

	if(groupname != ""){
		$.get(url, function(data) {
			//invalid Group name
			if(data == "false") {
				$("#inputGroupname").parent().addClass("has-feedback has-error");
				$("#invalidGroupnameMsg").show();
				$("#invalidGroupnameIcon").show();
				return false;
			}
			else {
				$("#inputGroupname").parent().removeClass("has-feedback has-error");
				$("#invalidGroupnameMsg").hide();
				$("#invalidGroupnameIcon").hide();
				$("#emptyGroupnameMsg").hide();
			}
		});
	}
	//blank group name
	else {
		$("#inputGroupname").parent().addClass("has-feedback has-error");
		$("#emptyGroupnameMsg").show();
		$("#invalidGroupnameIcon").show();
		return false;
	}
	return true;
}


/*
 * Validates add/modify group form
 */
function groupFormValidator(urlValidateGroupname) {
	$("#inputGroupname").focusout(function() {
		validateGroupnameAjax(urlValidateGroupname);
	});
}

/*
 * Validates if group form can  be submitted
 */
function groupFormCanBeSubmitted(urlValidateGroupname, isAddAction){

	//if action is modify, we don't need to validate group name and additional info
	if(!isAddAction) return true;

	var isValidGroupname = validateGroupnameAjax(urlValidateGroupname);

	if(isValidGroupname){
		return true;
	}

	return false;
}


/*
 * Function that goes to the server and checks if the profile name entered is valid or not
 */
function validateProfileNameAjax(urlValidateProfileName) {
	var profileName = $.trim($("#inputProfileName").val());
	var url = urlValidateProfileName + profileName + "/";

	$("#invalidProfileNameMsg").hide();
	$("#invalidProfileNameIcon").hide();
	$("#emptyProfileNameMsg").hide();

	if(profileName != ""){
		$.get(url, function(data){
			//invalid profile name
			if(data == "false"){
				$("#inputProfileName").parent().addClass("has-feedback has-error");
				$("#invalidProfileNameMsg").show();
				$("#invalidProfileNameIcon").show();
			}
			else {
				$("#inputProfileName").parent().removeClass("has-feedback has-error");
				$("#invalidProfileNameMsg").hide();
				$("#invalidProfileNameIcon").hide();
				$("#emptyProfileNameMsg").hide();
			}
		});
	}
	//blank profile name
	else {
		$("#inputProfileName").parent().addClass("has-feedback has-error");
		$("#emptyProfileNameMsg").show();
		$("#invalidProfileNameIcon").show();
		return false;
	}

	return true;
}

function validateProfileDescription(){
	var profileDescription = $.trim($("#inputDescription").val());

	if(profileDescription == ""){
		return false;
	}
	//valid additional info
	$("#inputDescription").parent().removeClass("has-feedback has-error");
	return true;
}

/*
 * Validates a username using AJAX
 */
function validateUsernameAjax(urlValidateUsername){
	var username = $.trim($("#inputUsername").val());
	var url = urlValidateUsername + username + "/";

	$("#invalidUsernameMsg").hide();
	$("#invalidUsernameIcon").hide();
	$("#emptyUsernameMsg").hide();

	if(username != ""){
		$.get(url, function(data) {
			//invalid username
			if(data == "false") {
				$("#inputUsername").parent().addClass("has-feedback has-error");
				$("#invalidUsernameMsg").show();
				$("#invalidUsernameIcon").show();
				return false;
			}
			else {
				$("#inputUsername").parent().removeClass("has-feedback has-error");
				$("#invalidUsernameMsg").hide();
				$("#invalidUsernameIcon").hide();
				$("#emptyUsernameMsg").hide();
			}
		});
	}
	//blank username
	else {
		$("#inputUsername").parent().addClass("has-feedback has-error");
		$("#emptyUsernameMsg").show();
		$("#invalidUsernameIcon").show();
		return false;
	}
	return true;
}

/*
 * Validates password
 */
function validatePassword(){
	var password = $.trim($("#inputPassword").val());

	if(password == ""){
		return true;
	}
	
	return true;
}

/*
 * Validates password confirmation
 */
function validatePasswordConf() {
	var passwordConf = $.trim($("#inputPasswordConfirmation").val());
	var password = $.trim($("#inputPassword").val());

if(passwordConf == "" && password == ""){
	return true;
}
else if(passwordConf == ""){
		$("#inputPasswordConfirmation").parent().addClass("has-feedback has-error");

		$("#invalidPasswordConfIcon").show();

		$("#emptyPasswordConfMsg").show();
		$("#notMatchPasswordMsg").hide();
		return false;

	}
	//password confirmation does not match password
	else if(passwordConf != password) {
		$("#inputPasswordConfirmation").parent().addClass("has-feedback has-error");

		$("#invalidPasswordConfIcon").show();

		$("#emptyPasswordConfMsg").hide();
		$("#notMatchPasswordConfMsg").show();
		return false;
	}
	//password length less than 5 characters
	else if(passwordConf.length < 5) {
		$("#inputPasswordConfirmation").parent().addClass("has-feedback has-error");

		$("#invalidPasswordConfIcon").show();

		$("#emptyPasswordConfMsg").hide();
		$("#notMatchPasswordConfMsg").hide();
		return false;
	}
	//valid password
	else {
		$("#inputPasswordConfirmation").parent().removeClass("has-feedback has-error");

		$("#invalidPasswordConfIcon").hide();
		$("#emptyPasswordConfMsg").hide();
		$("#notMatchPasswordConfMsg").hide();
	}
	return true;
}

/*
 * Validates email
 */
function validateEmail() {
	var emailRegex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;

	var email = $.trim($("#inputEmail").val());

	if(email != "" && !emailRegex.test(email)){
		$("#inputEmail").parent().addClass("has-feedback has-error");

		$("#invalidEmailIcon").show();
		$("#invalidEmailMsg").show();
		return false;
	}
	//valid additional info
	else {
		$("#inputEmail").parent().removeClass("has-feedback has-error");

		$("#invalidEmailIcon").hide();
		$("#invalidEmailMsg").hide();
	}
	return true;
}

/*
 * Validates add/modify user form
 */
function userFormValidator(urlValidateUsername) {

	$("#inputUsername").focusout(function() {
		validateUsernameAjax(urlValidateUsername);
	});

	$("#inputPassword").focusout(function() {
		validatePassword();
	});

	$("#inputPasswordConfirmation").focusout(function() {
		validatePasswordConf();
	});
	/*
	$("#inputEmail").focusout(function() {
		validateEmail();
	});*/
}

/*
 * Validates user form before submit it
 */
function userFormCanBeSubmitted(urlValidateUsername, isAddAction){
	var isValidUsername = true;
	var isValidPassword = true;
	var isValidPasswordConf = true;

	if(isAddAction) {
		isValidUsername = validateUsernameAjax(urlValidateUsername);
		isValidPassword = validatePassword();
		isValidPasswordConf = validatePasswordConf();
	}

	//var isValidEmail = validateEmail();

	if(isValidUsername && isValidPassword && isValidPasswordConf){
		return true;
	}

	return false;
}
