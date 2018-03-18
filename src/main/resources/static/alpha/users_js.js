var stompClient = null;

function connect() {
    //setConnected(true);
    stompClient = new WebSocket('ws://'+window.location.host+'/usersocket');
    stompClient.onmessage = function (data) {
        insertInterlocutorMessage(data.data.replace(/\n+/g, "</br>"));
        //showGreeting(data.data);
    };
    stompClient.onclose=function (data) {
        alert("The server broken down");
        //showGreeting(data.data);
    }
}

function sendName(name) {
    stompClient.send(JSON.stringify({'name': name}));
}


(function ($) {
    $(document).ready(function () {
        var $chatbox = $('.chatbox'),
            $chatboxTitle = $('.chatbox__title'),
            $chatboxCredentials = $('.chatbox__credentials');
        $chatboxTitle.on('click', function () {
            $chatbox.toggleClass('chatbox--tray');
        });
        $chatbox.on('transitionend', function () {
            if ($chatbox.hasClass('chatbox--closed')) $chatbox.remove();
        });

        $chatboxCredentials.on('submit', function (e) {
            e.preventDefault();
            $chatbox.removeClass('chatbox--empty');
        });

    });
})(jQuery);

function insertMyMessage(message) {
    var inner = "<div class=\"chatbox__body__message chatbox__body__message--right\">\n" +
        "            <img src=\"https://s3.amazonaws.com/uifaces/faces/twitter/arashmil/128.jpg\" alt=\"Picture\">\n" +
        "            <p>"+message+"</p>\n" +
        "        </div>";
    $(".chatbox__body").append(inner).scrollTop($(".chatbox__body").prop('scrollHeight'));
}

function insertInterlocutorMessage(message) {
    var inner = "<div class=\"chatbox__body__message chatbox__body__message--left\">\n" +
        "            <img src=\"https://s3.amazonaws.com/uifaces/faces/twitter/brad_frost/128.jpg\" alt=\"Picture\">\n" +
        "            <p>"+"\n"+message+"</p>\n" +
        "        </div>";
    $(".chatbox__body").append(inner).scrollTop($(".chatbox__body").prop('scrollHeight'));

}

function sendMessage() {
    var message = $('.chatbox__message').val();
   // alert(stompClient.CLOSED);
        stompClient.send(JSON.stringify({'message': message}))
    $('.chatbox__message').val('');
    insertMyMessage(message.replace(/\n+/g, "</br>"));
}
