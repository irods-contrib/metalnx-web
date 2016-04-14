/**
 * JS file that has an Ajax method that encapsulates all Ajax calls. In this encapsulated call,
 * we handle success, errors and session expired exceptions.
 */
function ajaxEncapsulation(url, method, params, successFunction, errorFunction, dataType, callbackFunctions){
    $.ajax({
        url: url,
        type: method,
        dataType: dataType,
        data: params,
        async: true,
        cache: false,
        success: successFunction,
        error: errorFunction,
        complete: function() {
            $(".alert-danger").delay(12000).fadeOut('slow');
            $(".alert-warning").delay(12000).fadeOut('slow');
            $(".alert-success").delay(12000).fadeOut('slow');
            $(".label-success").delay(4000).fadeOut('slow');
            $(".label-warning").delay(4000).fadeOut('slow');
            $(".label-danger").delay(4000).fadeOut('slow');
        },
        statusCode: {
            500: function(response){
                window.location= "/emc-metalnx-web/httpError/500/";
            },
            408: function(response){
                window.location= "/emc-metalnx-web/login/";
            },
            403: function(response){
                window.location= "/emc-metalnx-web/login/";
            },
            503: function(response){
                window.location= "/emc-metalnx-web/httpError/serverNotResponding/";
            }
        }		
    }).done(callbackFunctions);
}

function pageNotFound(response){
    alert(response);
}