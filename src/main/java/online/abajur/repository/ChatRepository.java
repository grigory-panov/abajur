package online.abajur.repository;

import online.abajur.domain.AbajurUser;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ChatRepository {

    private static final Map<String, AbajurUser> storage = Collections.synchronizedMap(new HashMap<String, AbajurUser>());
    private static int sequence = 0;

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

}
