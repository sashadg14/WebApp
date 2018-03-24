/*
(function () {
    var Message;
    Message = function (arg) {
        this.text = arg.text, this.message_side = arg.message_side;
        this.draw = function (_this) {
            return function () {
                var $message;
                $message = $($('.message_template').clone().html());
                $message.addClass(_this.message_side).find('.text').html(_this.text);
                $('.messages').append($message);
                return setTimeout(function () {
                    return $message.addClass('appeared');
                }, 0);
            };
        }(this);
        return this;
    };

    $(function () {
        var getMessageText, message_side, sendMessage;
        message_side = 'right';
        getMessageText = function () {
            var $message_input;
            $message_input = $('.message_input');
            return $message_input.val();
        };
        sendMessage = function (text) {
            var $messages, message;
            if (text.trim() === '') {
                return;
            }
            $('.message_input').val('');
            $messages = $('.messages');
            message_side = message_side === 'left' ? 'right' : 'left';
            message = new Message({
                text: text,
                message_side: message_side
            });
            message.draw();
            return $messages.animate({ scrollTop: $messages.prop('scrollHeight') }, 300);
        };
        $('.send_message').click(function (e) {
            return sendMessage(getMessageText());
        });
        /!*$('.message_input').keyup(function (e) {
            if (e.which === 13) {
                return sendMessage(getMessageText());
            }
        });*!/
        sendMessage('Hello Philip! :)');
        setTimeout(function () {
            return sendMessage('Hi Sandy! How are you?');
        }, 1000);
        return setTimeout(function () {
            return sendMessage('I\'m fine, thank you!');
        }, 2000);
    });
}.call(this));
*/
var stompClient = null;

function connect() {
    myMap=new Map();
    //setConnected(true);
    stompClient = new WebSocket('ws://' + window.location.host + '/agentsocket');
    stompClient.onmessage = function (data) {
        if ('SUCCESS: REGISTERED' === data.data.trim()) {
            ($(".container")).remove();
            $(".chat_window").append('<div class="top_menu">\n' +
                '        <div class="buttons">\n' +
                '<ul class="nav nav-tabs">\n' +
                '</ul>' +
                '        </div>\n' +
                '        <div class="title">Chat</div>\n' +
                '    </div>\n' +
                '<div class="tab-content"></div>');
        }
        else {
           // alert(data.data);
            var type;
            var obj = JSON.parse(data.data, function (key, value){
                if (key == "newUserName") {
                    //alert(data.data);
                    type=1;
                    return value;
                } else if (key == "message"){
                    type=2;
                  //  alert(value);
                    return value;
                } else if (key == "disconnected"){
                    type=3;
                    alert(myMap.get(value)+" disconnected");
                    $('#'+value+"tab").remove();
                    $('#'+value+"navtab").remove();
                    myMap.delete(value);
                    return value;
                } else {
                    return value;
                }
            });
            if (type==1) {
                myMap.set(obj.id,obj.newUserName);
                addNewTab(""+obj.id,obj.newUserName);
                //insertInterlocutorMessage(obj.newUserName);
            } else if(type==2){
                insertInterlocutorMessage(""+obj.id,obj.message.replace(/\n+/g, "</br>"));
            }
        }
        //showGreeting(data.data);
    };
    stompClient.onclose = function (data) {
        alert("The server broken down");
        //showGreeting(data.data);
    }
}

function addNewTab(userId,userName) {
    $(".nav-tabs").append('<li><a data-toggle="tab" id="'+userId+'navtab" href="#' + userId + 'tab">' + userName + '</a></li>\n'
    );
    createNewChatWindow(userId)
}

function createNewChatWindow(userId){
    var a =
        ' <div id="' + userId + 'tab" class="tab-pane fade">' +
        '   <ul id="' + userId + 'Messages" class="messages">\n' +
        '    </ul>\n' +
        '    <div class="bottom_wrapper clearfix">\n' +
        '        <div class="message_input_wrapper"><input class="message_input" id="' + userId + 'Input" placeholder="Type your message here..."/></div>\n' +
        '        <div class="send_message" id="' + userId + 'Send" onclick="sendMessage(\''+userId+'\')">\n' +
        '            <div class="icon"></div>\n' +
        '            <div class="text">Send</div>\n' +
        '        </div>\n' +
        '    </div>' +
        '</div>';
    $(".tab-content").append(a);
}

function sendName(name) {
    stompClient.send(JSON.stringify({'name': name}));
}

function insertMyMessage(userId,message) {
    var inner = '<li class="message right appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <p>' + message + '</p>\n' +
        '            </div>\n' +
        '        </li>';
    var id=userId+'Messages';
    $('#'+id).append(inner).scrollTop($('#'+userId+'Messages').prop('scrollHeight'));
}

function insertInterlocutorMessage(userId,message) {
    var inner = '<li class="message left appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <p>' + message + '</p>\n' +
        '            <\div>\n' +
        '        </li>';
    var id=userId+'Messages';

    $('#'+id).append(inner).scrollTop($('#'+userId+'Messages').prop('scrollHeight'));


}

function sendMessage(userId) {
    var message = $('#'+userId+'Input').val();
    var struct;
    // alert(stompClient.CLOSED);
    stompClient.send(JSON.stringify({'message': {'id':userId, 'message': message}}));
    $('#'+userId+'Input').val('');
    //alert(message);
    insertMyMessage(userId,message.replace(/\n+/g, "</br>"));
}

function getMessageText() {
    var $message_input;
    $message_input = $('.message_input');
    return $message_input.val();
};