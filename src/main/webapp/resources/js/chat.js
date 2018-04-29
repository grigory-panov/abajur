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
        $('.send-button').on('click', sendMessage);
        $('.img-button').on('click', selectImage);
        var inputFile = $("<input type='file' accept='.jpg, .png, .jpeg, .gif|images/*'/>").change(function () {
            if (this.files && this.files[0]) {
                $('.progress-bar').attr('aria-valuenow', '0').width('0%').removeClass('bg-danger').text('').hide();
                $('#uploadComment').val('');
                $('.modal').modal('show');
                var size = this.files[0].size || this.files[0].fileSize;
                if (size > 10000000) { // 10MB
                    showUploadError("Max file size : 9Mb");
                     $('.modal').find('.btn-primary').attr('disabled', 'disabled');
                }else{
                    $('.modal').find('.btn-primary').removeAttr('disabled');
                    var reader = new FileReader();
                    var imageSrc;
                    reader.onload = function (e) {
                        imageSrc = e.target.result;
                        $('.modal').find('img').attr('src', imageSrc);
                    };
                    reader.readAsDataURL(this.files[0]);
                    var formData = new FormData();
                    formData.append("file", this.files[0]);

                    $('.modal').find('.btn-primary').off('click.chat').on('click.chat', function(){
                        if($('#toGalery').prop('checked')){
                            formData.append('toGalery', 'on');
                        }
                        formData.append("text", $('#uploadComment').val());
                        var sendFileXhr = $.ajax({
                            xhr: function () {
                                var xhr = new window.XMLHttpRequest();
                                xhr.upload.addEventListener("progress", function (evt) {
                                    $('.progress-bar').show();
                                    if (evt.lengthComputable) {
                                        var percentComplete = Math.ceil(evt.loaded / evt.total) * 100;
                                        $(".progress-bar").width(percentComplete + '%').attr('aria-valuenow', percentComplete);
                                    }
                                }, false);
                                return xhr;
                            },
                            processData: false,
                            contentType: false,
                            dataType: "json",
                            cache: false,
                            type: 'POST',
                            url: SETTINGS.contextPath + "sendfile",
                            data: formData,
                            success: function (data) {
                                if (data.result === "ERROR") {
                                    showUploadError(data.message);
                                } else {
                                    $('.modal').modal('hide');
                                }
                            },
                            error: function (jqXHR, textStatus, errorThrown) {
                                showUploadError(textStatus);
                            }
                        });
                    });
                }
            }
        });

        function showUploadError(errorText){
             $('.progress-bar').width('100%').addClass('bg-danger').attr('aria-valuenow', '100').text(errorText).show();
        }

        function connectSocket() {
            $('.alert').text('Присоединяемся к чату...').removeClass('collapse');
            socket = new SockJS(SETTINGS.contextPath + "websocket", {}, {transports: ["websocket"]});
            stompClient = Stomp.over(socket);
            stompClient.connect({},
                    connectedCallback,
                    errorCallback);
        }

        function createWindow() {

            $("#message").off(".chat").on("keyup.chat", function (e) {

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

            $("#message").on('input', changeSendButtonStatus);

        }

        function changeSendButtonStatus(){
            if($(this).val().length == 0){
                $('.send-button').addClass('disabled');
            }else{
                $('.send-button').removeClass('disabled');
            }
        }


        function selectImage(){
            $('.modal').find('img').attr('src', '');
            inputFile.val('');
            inputFile.click();
        }
        function sendMessage() {
            var input = $("#message");
            if (input.val().length > 0 && stompClientConnected) {
                stompClient.send("/topic/teamChat", {
                    'edit': input.hasClass("edit"),
                    'msg-id': input.data('edit-id')
                }, input.val());
                input.val("");
                stopEditLastMessage();
            }
        }

        function loadHistory(messageArea, limit, offset) {
            var data = {};
            data.limit = limit;
            data.offset = offset;
            $('.alert').text('Загружаем историю...').removeClass('collapse');
            $.ajax({
                url: SETTINGS.contextPath + "history",
                async: true,
                method: "GET",
                dataType: "json",
                data: data,
                success: function (jsonResponse) {
                    if(!$('chat-window-viewport').hasClass('h-100')){
                        $("chat-window-viewport").addClass("h-100");
                    }
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
                    $('.alert').addClass('collapse');
                    messageArea.prepend(dummy.children());
                    baguetteBox.run('.chat-window-viewport', {filter: /full$/});
                    if (areaCleared) {
                        scrollViewToBottom();
                    }

                },
                error: function () {
                    $('.alert').text('Не удалось загрузить историю');
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

        function addFile(messageArea, author, authorName, fullText, fileId, id, date) {
             var link = $("<a/>").addClass("lithbox").attr("href", SETTINGS.contextPath + "storage/"+fileId+"/full");
             var img = $("<img/>").addClass("figure-img img-fluid rounded").attr('src', SETTINGS.contextPath + "storage/"+fileId+"/medium");
             link.append(img);
             var figure = $('<figure/>').addClass('figure');
             var caption = $('<figcaption/>').addClass('figure-caption').text(fullText);
             figure.append(link);
             figure.append(caption);
             var p = $('<div/>').addClass("p-1").attr('data-id', id);
             p.append(figure);

             var dateDiv = $("<div/>").addClass("message-time p-1").text(dateToString(date));
             var newBubble = createBubble(author, authorName);
             newBubble.append(dateDiv);
             newBubble.append(p);
             messageArea.append($("<div/>").addClass('p-1').append(newBubble));
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
                    if(text.length > 0){
                        var p = $('<div/>').addClass("p-1").text(text).attr('data-id', id);
                        if(type === "edit"){
                            p.addClass("edited-message");
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
                    }
                });
            }
            //Прокрутка в конец списка только если мы в конце
            if (atBottom) {
                scrollViewToBottom();
            }

        }


        function connectedCallback() {
            stompClientConnected = true;
            $('.alert').addClass('collapse');
            retryCount = 0;
            historyOffset = 0;
            openChat();
        }

        function errorCallback(message) {
            $('.alert').text('Не удалось присоединиться к чату');
            if(retryCount >= MAX_RETRY_COUNT){
                $('.alert').text('Не удалось присоединиться к чату, для повторной попытки обновите страницу');
            }
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
            var destination = "/topic/teamChat";

            loadHistory(messageArea, initialHistoryMessageLoadCount, historyOffset);

            stompClient.subscribe(destination, function (message) {
                if (message.headers['action-id']) {
                    if (message.headers['action-id'] === 'JOIN') {
                        //join(messageArea, message.headers['user-id'], message.body);
                    }
                } else {
                    if (message.headers.edit === 'true') {
                        changeMessage(message.headers['msg-id'], message.headers['user-id'], message.body);
                    } else {
                        addMessage(messageArea, message.headers['user-id'], message.headers['user-name'], message.body,
                                message.headers.type, message.headers['file-id'], message.headers['msg-id'], new Date().getTime());
                        if(message.headers.type == 'file'){
                            baguetteBox.run('.chat-window-viewport', {filter: /full$/});
                        }
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
                if(text.length > 0){
                    var p = $('<div/>').text(text);
                    p.attr('data-id', id);
                    p.addClass('edited-message p-1');
                    twemoji.parse(p[0]);
                    if (old !== null) {
                        p.insertAfter(old);
                    } else {
                        bubble.append(p);
                    }
                    old = p;
                }
            });
        }

        function editLastMessage() {
            var input = $("#message");
            var area = $("#chat-history");
            var lastMessage = area.find("div[data-author='" + SETTINGS.currentUser + "'] > div").last();
            if (lastMessage.length === 0 || lastMessage.find('figure').length > 0) {
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
