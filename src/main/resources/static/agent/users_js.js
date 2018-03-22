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
    //setConnected(true);
    stompClient = new WebSocket('ws://'+window.location.host+'/agentsocket');
    stompClient.onmessage = function (data) {
        insertInterlocutorMessage(data.data.replace(/\n+/g, "</br>"));
        if('SUCCESS: REGISTERED' === data.data.trim())
            clearWindow1();
        //showGreeting(data.data);
    };
    stompClient.onclose=function (data) {
        alert("The server broken down");
        //showGreeting(data.data);
    }
}

function clearWindow1() {
    ($(".container")).remove();

    var a='<div class="top_menu">\n' +
        '        <div class="buttons">\n' +
        '            <div class="button close"></div>\n' +
        '            <div class="button minimize"></div>\n' +
        '            <div class="button maximize"></div>\n' +
        '        </div>\n' +
        '        <div class="title">Chat</div>\n' +
        '    </div>\n' +
        '    <ul class="messages">\n' +
        '        <li class="message left appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <div class="text">asdfasdfa</div>\n' +
        '            </div>\n' +
        '        </li>\n' +
        '        <li class="message left appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <div class="text">asdfasdfa</div>\n' +
        '            </div>\n' +
        '        </li>\n' +
        '\n' +
        '        <li class="message right appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <div class="text">Hi Sandy! How are you?</div>\n' +
        '            </div>\n' +
        '        </li>\n' +
        '\n' +
        '    </ul>\n' +
        '    <div class="bottom_wrapper clearfix">\n' +
        '        <div class="message_input_wrapper"><input class="message_input" placeholder="Type your message here..."/></div>\n' +
        '        <div class="send_message" onclick="sendMessage()">\n' +
        '            <div class="icon"></div>\n' +
        '            <div class="text">Send</div>\n' +
        '        </div>\n' +
        '    </div>';

    $(".chat_window").append(a);
}

function sendName(name) {
    stompClient.send(JSON.stringify({'name': name}));
}

function insertMyMessage(message) {
    var inner = '<li class="message right appeared">\n' +
        '            <div class="avatar"></div>\n' +
        '            <div class="text_wrapper">\n' +
        '                <p>'+message+'</p>\n' +
        '            </div>\n' +
        '        </li>';
    $(".messages").append(inner).scrollTop($(".messages").prop('scrollHeight'));
}

function insertInterlocutorMessage(message) {
    var inner = "<li class=\"message left appeared\">\n" +
        "            <div class=\"avatar\"></div>\n" +
        "            <div class=\"text_wrapper\">\n" +
        "                <p>"+message+"</p>\n" +
        "            </div>\n" +
        "        </li>";
    $(".messages").append(inner).scrollTop($(".messages").prop('scrollHeight'));

}

function sendMessage() {

    var message = $('.message_input').val();
    // alert(stompClient.CLOSED);
    stompClient.send(JSON.stringify({'message': message}))
    $('.message_input').val('');
    insertMyMessage(message.replace(/\n+/g, "</br>"));
}
    function getMessageText() {
    var $message_input;
    $message_input = $('.message_input');
    return $message_input.val();
    };