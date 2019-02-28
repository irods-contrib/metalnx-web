 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */


/**
 * JS file that has an Ajax method that encapsulates all Ajax calls. In this encapsulated call,
 * we handle success, errors and session expired exceptions.
 */
function ajaxEncapsulation(url, method, params, successFunction, errorFunction, dataType, contentType, callbacks){
    if (contentType == null || typeof contentType === 'undefined') {
        contentType = "application/x-www-form-urlencoded; charset=UTF-8";
    }

    $.ajax({
        url: url,
        type: method,
        contentType: contentType,
        dataType: dataType,
        data: params,
        async: true,
        cache: false,
        // wrap success to check for invalid session or login exception errors
        success: function (data, status, jqXHR) {

          if (String(data).search("XXXLOGINXXX") >= 0) {
                console.log("current location = " + window.location);
                console.log("location path =" + window.location.pathname);
                window.location= "/metalnx/login/" + window.location.search + "&ajaxOrigPath=" + window.location.pathname;
                return false;
          }

          if (successFunction) {
            	   successFunction(data, status, jqXHR);
          }

        },
        error: errorFunction,
        complete: function() {
            $(".alert-danger").delay(12000).fadeOut('slow');
            $(".alert-warning").delay(12000).fadeOut('slow');
            $(".alert-success").delay(12000).fadeOut('slow');
            $("table .label-success").delay(4000).fadeOut('slow');
            $("table .label-warning").delay(4000).fadeOut('slow');
            $("table .label-danger").delay(4000).fadeOut('slow');
        },
        statusCode: {
            500: function(response){
                window.location= "/metalnx/httpError/500/";
            },
            408: function(response){
                window.location= "/metalnx/login/";
            },
            403: function(response){
                window.location= "/metalnx/login/";
            },
            503: function(response){
                window.location= "/metalnx/httpError/serverNotResponding/";
            }
        }
    }).done(callbacks);
}

function pageNotFound(response){
    alert(response);
}
