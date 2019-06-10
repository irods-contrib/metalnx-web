 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

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
