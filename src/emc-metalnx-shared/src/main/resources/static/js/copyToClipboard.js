 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 
// TODO: replace with clipboard.js
function setCopyTicketBtn(){
    var copyTicketBtn = document.querySelector('.copyTicketBtn');
    copyTicketBtn.addEventListener('click', function(event) {
        copyTextToClipboard();
    });
}

function copyTextToClipboard() {
    var ticketInfo = document.querySelector('#newTicketInfoCopy');
    var range = document.createRange();
    range.selectNode(ticketInfo);
    window.getSelection().addRange(range);
    document.execCommand('copy');
    window.getSelection().removeAllRanges();
}