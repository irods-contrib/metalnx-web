/*
 *    Copyright (c) 2015-2017 Dell EMC
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

/**
 * JS file that has an Ajax method that encapsulates all Ajax calls. In this encapsulated call,
 * we handle success, errors and session expired exceptions.
 */
function ajaxEncapsulation(url, method, params, successFunction, errorFunction, dataType, contentType, callbacks){
    if (contentType == null || typeof contentType === 'undefined') {
        contentType = "application/x-www-form-urlencoded; charset=UTF-8";
    }
    console.log("calling ajaxEncapsulation - ajax.js");
    $.ajax({
        url: url,
        type: method,
        contentType: contentType,
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
            $("table .label-success").delay(4000).fadeOut('slow');
            $("table .label-warning").delay(4000).fadeOut('slow');
            $("table .label-danger").delay(4000).fadeOut('slow');
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
    }).done(callbacks);
}

function pageNotFound(response){
    alert(response);
}