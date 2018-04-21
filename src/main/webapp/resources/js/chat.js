"use strict";
/*global SETTINGS:false, Stomp:false, SockJS:false, performance:false, twemoji:false, CURRENCY:false, COUNTRY:false, convertDateDiff: false*/
if (window.jQuery) {

    $(document).ready(function () {

        var MAX_RETRY_COUNT = 12,
                socket,
                stompClient,
                stompClientConnected = false,
                retryCount = 0,
                currentUser,
                timerId = 0,
                initialHistoryMessageLoadCount = 100,
                historyOffset = 0;


        createWindow();
        connectSocket();

        function connectSocket() {

            socket = new SockJS(SETTINGS.contextPath + "websocket", {}, {transports: ["websocket"]});
            stompClient = Stomp.over(socket);
            stompClient.connect({},
                    connectedCallback,
                    errorCallback);
        }

        function createWindow() {

            $("main").addClass("h-100");

            $("#message").off(".chat").on("keydown.chat", function (e) {

                if (e.which === 13 && !e.shiftKey) {
                    e.preventDefault();
                    sendMessage();
                }
                if (e.which === 38 && $(this).val() === "") {// up arrow and nothing entered in field
                    editLastMessage();
                }
                if (e.which === 27) {
                    stopEditLastMessage();
                }

            });

        }

        function sendMessage() {
            var input = $("#message");
            if (input.val()) {
                if (stompClientConnected) {
                    stompClient.send("/topic/teamChat", {
                        'edit': input.hasClass("edit"),
                        'msg-id': input.data('edit-id')
                    }, input.val());
                    input.val("");
                }
                // prevent sending typing message after user send message
                stopEditLastMessage();
            }
        }

        function loadHistory(messageArea, limit, offset) {
            var data = {};
            data.limit = limit;
            data.offset = offset;
            $.ajax({
                url: SETTINGS.contextPath + "history",
                async: true,
                method: "GET",
                dataType: "json",
                data: data,
                success: function (jsonResponse) {

                    var response = jsonResponse.history;
                    var areaCleared = historyOffset === 0;
                    if (areaCleared) {
                        messageArea.empty();
                    }
                    historyOffset += initialHistoryMessageLoadCount;
                    $(".show-full-chat").parent().remove();
                    //сохраняем всю историю в невидимый контейнер
                    var dummy = $("<div/>");

                    if (jsonResponse.totalMessages > historyOffset) {
                        addMoreHistoryLink(dummy, initialHistoryMessageLoadCount, historyOffset);
                    }
                    for (var i = 0; i < response.length; i++) {
                        addMessage(dummy, response[i].author, response[i].authorName, response[i].text, response[i].type, response[i].fileId, response[i].id, response[i].date*1000);
                    }
                    messageArea.prepend(dummy.children());
                    if (areaCleared) {
                        scrollViewToBottom();
                    }

                },
                error: function () {
                    messageArea.append($("<div/>").text("cannot load history"));
                }
            });
        }

        function addMoreHistoryLink(messageArea, limit, offset) {

            var a = $("<a/>").text("Показать историю");
            a.on("click.chat", function () {
                loadHistory($("#chat-history"), limit, offset);
            });
            var row = ($('<div/>').addClass("mt-auto").append($("<p/>").addClass("show-full-chat").append(a)));
            messageArea.append(row);

        }

        function createBubble(author, authorName) {
            var bubble = $("<div/>").addClass("bubble p-1").attr("data-author", author);
            if(author == SETTINGS.currentUser){
                bubble.addClass("my");
            }
            var title = $("<div/>").addClass("message-author p-1").text(authorName);
            bubble.append(title);
            return bubble;
        }

        function addFile(messageArea, author, authorName, fullText, type, fileId, id, date) {
             var link = $("<a/>").attr("href", SETTINGS.contextPath + "attachment/" + fileId).text(fullText);
             var p = $('<div/>').addClass("p-1").attr('data-id', id);
             p.append(link);
             preloadImage(link, false);
             var dateDiv = $("<div/>").addClass("message-time p-1").text(dateToString(date));
             var newBubble = createBubble(author, authorName);
             newBubble.append(dateDiv);
             newBubble.append(p);
             messageArea.append($("<div class='p-1'>").append(newBubble));
        }

        function dateToString(date){
             var dateMessage = new Date(date);
             var now = new Date();
             if(now.getYear() === dateMessage.getYear()
                 && now.getMonth() === dateMessage.getMonth()
                 && now.getDay() === dateMessage.getDay()){
                 return dateMessage.toLocaleTimeString();
             }
             return dateMessage.toLocaleDateString();
        }

        function addMessage(messageArea, author, authorName, fullText, type, fileId, id, date) {
            var atBottom = isViewScrolledToBottom();

            if(type === "file"){
                addFile(messageArea, author, authorName, fullText, fileId, id, date);
            }else{

                fullText.split('\n').forEach(function (text) {
                    var p = $('<div/>').addClass("p-1").text(text).attr('data-id', id);
                    if(type === "edit"){
                        p.addClass("edited_message");
                    }
                    twemoji.parse(p[0]);

                    var dateDiv = $("<div/>").addClass("message-time p-1").text(dateToString(date));
                    var lastBubble = messageArea.find('.bubble').last();
                    var previousAuthor = lastBubble.attr('data-author');
                    if (lastBubble.length > 0 && author === previousAuthor) {
                        lastBubble.append(dateDiv);
                        lastBubble.append(p);
                    } else {
                        var newBubble = createBubble(author, authorName);
                        newBubble.append(dateDiv);
                        newBubble.append(p);
                        messageArea.append($("<div class='p-1'>").append(newBubble));
                    }
                });
            }
            //Прокрутка в конец списка только если мы в конце
            if (atBottom) {
                scrollViewToBottom();
            }

        }

        function preloadImage(link, needScrollToBottom) {
            var img = $("<img/>").addClass("chat-image-preview").attr("alt", link.text());
            img.one("load", function () {
                img.attr("title", img[0].naturalWidth + 'x' + img[0].naturalHeight);
                link.text("").append(img);
                if (needScrollToBottom) {
                    scrollViewToBottom();
                }
            }).each(function () {
                if (this.complete && $(this).width > 0) {
                    $(this).load();
                }
            });
            img.attr("src", link.attr("href"));
            link.attr("download", "download");
        }


        function connectedCallback() {
            stompClientConnected = true;
            retryCount = 0;
            historyOffset = 0;
            openChat();
        }

        function errorCallback(message) {
            stompClientConnected = false;
            // error handler - handle any error. Usually this is disconnect event, but can be also subscribe error
            if (message.startsWith && message.startsWith('Whoops! Lost connection to')) {
                stompClient.disconnect(function () {
                    socket.close();
                    console.log("client disconnected");
                    clearTimeout(timerId);
                    retryCount++;
                    if (retryCount < MAX_RETRY_COUNT) {
                        console.log('retry count: ' + retryCount + ' reconnect in ' + (5 * retryCount) + ' sec');
                        timerId = setTimeout(connectSocket, 5000 * retryCount);
                    }
                });

            }
        }

        function openChat() {

            var messageArea = $("#chat-history");

            messageArea.empty();
            messageArea.append($("<div/>").addClass('spinner'));

            var destination = "/topic/teamChat";

            loadHistory(messageArea, initialHistoryMessageLoadCount, historyOffset);

            stompClient.subscribe(destination, function (message) {
                if (message.headers['action-id']) {
                    if (message.headers['action-id'] === 'TYPE') {
                        playTyping(message.headers['user-id']);
                    } else if (message.headers['action-id'] === 'JOIN') {
                        join(messageArea, message.headers['user-id'], message.body);
                    }
                } else {
                    if (message.headers.edit === 'true') {
                        changeMessage(message.headers['msg-id'], message.headers['user-id'], message.body);
                    } else {
                        addMessage(messageArea, message.headers['user-id'], message.headers['user-name'], message.body,
                                message.headers.type, message.headers['file-id'], message.headers['msg-id'], new Date().getTime());
                    }
                }
            });
        }

        function scrollViewToBottom() {
            var view = $('main');
            view.scrollTop(view.prop("scrollHeight"));
        }

        function isViewScrolledToBottom() {
            var view = $('main');
            return view.scrollTop() + view.innerHeight() >= view.prop("scrollHeight");
        }


        function changeMessage(id, author, newText) {
            var old = $("#chat-history").find("div[data-author='" + author + "'] > div[data-id='" + id + "']").first();
            if (old.length === 0) {
                return;
            }
            var bubble;
            if (old.prev("div").length !== 0) {
                old = old.prev("div");
            } else {
                bubble = old.parent();
                old = null;
            }

            $("#chat-history").find("div[data-author='" + author + "'] > div[data-id='" + id + "']").remove();
            newText.split('\n').forEach(function (text) {
                var p = $('<div/>').text(text);
                p.attr('data-id', id);
                p.addClass('edited_message p-1');
                twemoji.parse(p[0]);
                if (old !== null) {
                    p.insertAfter(old);
                } else {
                    bubble.append(p);
                }
                old = p;
            });
        }

        function editLastMessage() {
            var input = $("#message");
            var area = $("#chat-history");
            var lastMessage = area.find("div[data-author='" + SETTINGS.currentUser + "'] > div").last();
            if (lastMessage.length === 0 || lastMessage.hasClass('close_message')) {
                return;
            }
            var lastMessId = lastMessage.data('id');
            if (!input.hasClass("edit")) {
                input.addClass("edit");
            }
            input.data('edit-id', lastMessId);

            var lastMessageArray = area.find("div[data-author='" + SETTINGS.currentUser + "'] > div[data-id='" + lastMessId + "']");
            var text = [];
            $.each(lastMessageArray, function (ind, obj) {
                text.push($(obj).text());
            });
            input.val(text.join('\n'));
        }

        function stopEditLastMessage() {
            var input = $("#message");
            if (input.hasClass("edit")) {
                input.removeClass("edit");
                input.data('edit-id', null);
                input.val("");
            }
        }

    });



}
