package online.abajur.chat;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatMessage;
import online.abajur.service.ChatService;
import online.abajur.service.ImageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;
import org.springframework.messaging.support.GenericMessage;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

public class InboundChannelInterceptor extends ChannelInterceptorAdapter {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ImageService imageService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

        switch (sha.getCommand()) {
            case CONNECT:
                return handleConnect(sha, message);
            case SUBSCRIBE:
                return handleSubscribe(sha, message);
            case SEND:
                return handleSend(sha, message);
            default:
                return message;
        }

    }

    private Message<?> handleSubscribe(StompHeaderAccessor sha, Message<?> message) {
        if("/topic/teamChat".equals(sha.getDestination()) && chatService.userValid((String) sha.getSessionAttributes().get("abajur_user"))){
            AbajurUser user = chatService.getUser((String) sha.getSessionAttributes().get("abajur_user"));
            sha.removeHeader("user-id");
            sha.removeHeader("user-name");
            sha.addNativeHeader("user-id", user.getUid());
            sha.addNativeHeader("user-name", user.getName());
            return new GenericMessage<>(message.getPayload(), sha.getMessageHeaders());
        }
        return null;
    }

    private Message<?> handleConnect(StompHeaderAccessor sha, Message<?> message) {
        if(chatService.userValid((String) sha.getSessionAttributes().get("abajur_user"))){
            return message;
        }
        return null;
    }

    private Message<?> handleSend(StompHeaderAccessor sha, Message<?> message) {
        if("/topic/teamChat".equals(sha.getDestination()) && chatService.userValid((String) sha.getSessionAttributes().get("abajur_user"))){
            AbajurUser user = chatService.getUser((String) sha.getSessionAttributes().get("abajur_user"));
            sha.removeHeader("user-id");
            sha.removeHeader("user-name");
            sha.addNativeHeader("user-id", user.getUid());
            sha.addNativeHeader("user-name", user.getName());
            ChatMessage chatMessage = new ChatMessage();
            if("true".equalsIgnoreCase(sha.getFirstNativeHeader("edit")) &&
                    StringUtils.isNumeric(sha.getFirstNativeHeader("msg-id"))){
                chatMessage = chatService.getMessageByIdAndUserId(Long.valueOf(sha.getFirstNativeHeader("msg-id")), user.getUid());
                if(chatMessage != null){
                    chatMessage.setText(new String((byte[])message.getPayload(), StandardCharsets.UTF_8));
                    chatMessage.setType("edit");
                    chatService.saveMessage(chatMessage);
                }
            }else{
                chatMessage.setText(new String((byte[])message.getPayload(), StandardCharsets.UTF_8));
                chatMessage.setAuthor(user.getUid());
                chatMessage.setAuthorName(user.getName());
                chatMessage.setDate(ZonedDateTime.now());
                chatService.saveMessage(chatMessage);
                sha.setNativeHeader("msg-id", String.valueOf(chatMessage.getId()));
            }
            return new GenericMessage<>(message.getPayload(), sha.getMessageHeaders());
        }
        return null;
    }
}
