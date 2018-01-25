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

function serializeTicketForm(){
    if ($("#inputUsesLimit").val()==''){
        $("#inputUsesLimit").val(0);
    }
    if ($("#inputWriteByteLimit").val()==''){
        $("#inputWriteByteLimit").val(0);
    }
    if ($("#inputWriteFileLimit").val()==''){
        $("#inputWriteFileLimit").val(0);
    }

    var form = $("#ticketFormPanel form").serializeArray();
    var ticket = {};
    ticket['hosts'] = [];
    for (var i = 0; i < form.length; i++) {
        var name = form[i]['name'];
        var values = form[i]['value'].split(',');

        if (name == 'users' || name == 'groups') {
            ticket[name] = [];
            for (var v = 0; v < values.length; v++) {
                ticket[name].push(values[v]);
            }
        }
        else if(name == 'hosts'){
            ticket[name].push(values[0]);
        }
        else {
            ticket[name] = values[0];
        }
    }
    return ticket;
}

function removeHost(hostname){
    var li_id = 'li_' + hostname;
    var inputHost_id = 'input_' +  hostname;


    var li = document.getElementById(li_id);
    var inputHost =  document.getElementById(inputHost_id);
    var ul = li.parentElement;
    var panelHosts = inputHost.parentElement;

    ul.removeChild(li);
    ul.removeChild(inputHost);

}
