package online.abajur.service;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatHistory;
import online.abajur.domain.ChatMessage;
import online.abajur.repository.ChatRepository;
import online.abajur.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SettingsRepository settingsRepository;


    public boolean inviteValid(String invite) {
        return settingsRepository.getInviteCode().equals(invite);
    }

    public AbajurUser saveUser(String secretIid, String name) {
        return chatRepository.saveUser(secretIid, name);
    }

    public boolean userValid(String secretId) {
        return chatRepository.hasSecretId(secretId);
    }

    public AbajurUser getUser(String secretId) {
        return chatRepository.getUserBySecretId(secretId);
    }
    public AbajurUser getUserById(String uid) {
        return chatRepository.getUserById(uid);
    }

    public ChatHistory getHistory(int limit, int offset) {
        return chatRepository.getHistory(limit, offset);
    }

    public void saveMessage(ChatMessage chatMessage){
        chatRepository.saveMessage(chatMessage);
    }

    public ChatMessage getMessageByIdAndUserId(Long id, String userId) {
        ChatMessage cm =  chatRepository.getMessageById(id);
        if(cm!= null && cm.getAuthor().equals(userId)){
            return cm;
        }
        return null;
    }

    public ChatMessage getMessageByImageId(String uid) {
        return chatRepository.getMessageByImageId(uid);
    }
}
