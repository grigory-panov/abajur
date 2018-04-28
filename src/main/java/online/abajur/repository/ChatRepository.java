package online.abajur.repository;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatHistory;
import online.abajur.domain.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ChatRepository {

    private static final Map<String, AbajurUser> storage = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Long, ChatMessage> storageMessages = Collections.synchronizedMap(new LinkedHashMap<>());
    private static int sequence = 0;
    private static long sequenceMessages = 0;

    public AbajurUser saveUser(String secretId, String name) {
        AbajurUser user = new AbajurUser();
        user.setUid(String.valueOf(++sequence));
        user.setSecretId(secretId);
        user.setName(name);
        storage.put(secretId, user);
        return user;
    }

    public boolean hasSecretId(String uid) {
        return storage.containsKey(uid);
    }

    public AbajurUser getUserBySecretId(String uid) {
        if(storage.containsKey(uid)){
            return storage.get(uid);
        }
        return null;
    }

    public AbajurUser getUserById(String uid) {
        return storage.values().stream().filter(user -> user.getUid().equals(uid)).findFirst().orElse(null);
    }

    public ChatHistory getHistory(int limit, int offset) {
        ChatHistory history = new ChatHistory();
        history.setTotalMessages(storageMessages.size());
        int endInd = limit + offset;
        if(endInd >= storageMessages.size()){
            endInd = storageMessages.size();
        }
        if(endInd < offset){
            endInd = offset;
        }
        List<ChatMessage> h = new ArrayList<>(storageMessages.values());
        Collections.reverse(h);
        List<ChatMessage> chunk = h.subList(offset, endInd);
        Collections.reverse(chunk);
        history.setHistory(chunk);
        return history;
    }

    public void saveMessage(ChatMessage chatMessage){
        if(chatMessage.getId() == null){
            chatMessage.setId(++sequenceMessages);
        }
        storageMessages.put(chatMessage.getId(), chatMessage);
    }

    public ChatMessage getMessageById(Long id) {
        return storageMessages.get(id);
    }

    public ChatMessage getMessageByImageId(String uid) {
        return storageMessages.values().stream().filter( m -> uid.equals(m.getFileId())).findFirst().orElse(null);
    }
}
