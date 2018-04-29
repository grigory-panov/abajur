package online.abajur.repository;

import online.abajur.domain.AbajurUser;
import online.abajur.domain.ChatHistory;
import online.abajur.domain.ChatMessage;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional
public class ChatRepository {

    private final JdbcTemplate template;

    public ChatRepository(DataSource ds) {
        template = new JdbcTemplate(ds);
    }

    private static ChatMessage mapChatMessage(ResultSet resultSet, int i) throws SQLException {
        ChatMessage message = new ChatMessage();
        message.setId(resultSet.getLong("id"));
        message.setType(resultSet.getString("type"));
        message.setText(resultSet.getString("text"));
        message.setDate(resultSet.getTimestamp("create_date").toInstant().atZone(ZoneId.systemDefault()));
        message.setAuthor(resultSet.getString("author"));
        message.setAuthorName(resultSet.getString("author_name"));
        message.setFileId(resultSet.getString("file_id"));
        return message;
    }

    public AbajurUser saveUser(String secretId, String name) {
        AbajurUser user = new AbajurUser();
        user.setUid(String.valueOf(template.queryForObject("select NEXT VALUE FOR SEQ_USER", Long.class)));
        user.setSecretId(secretId);
        user.setName(name);
        template.update("insert into abajur_user(id, secret, name) values(?, ?, ?)", user.getUid(), user.getSecretId(), user.getName());
        return user;
    }

    public boolean hasSecretId(String uid) {
        return template.queryForObject("select count(*) from abajur_user where secret = ?", Integer.class, uid) > 0;
    }

    public AbajurUser getUserBySecretId(String secretUid) {
        try {
            return template.queryForObject("select * from abajur_user where secret = ?", (resultSet, i) -> {
                AbajurUser user = new AbajurUser();
                user.setUid(String.valueOf(resultSet.getLong("id")));
                user.setSecretId(resultSet.getString("secret"));
                user.setName(resultSet.getString("name"));
                return user;
            }, secretUid);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

    public ChatHistory getHistory(int limit, int offset) {
        ChatHistory history = new ChatHistory();
        history.setTotalMessages(template.queryForObject("select count(*) from chat_message", Integer.class));
        List<ChatMessage> list = template.query("select * from chat_message order by id desc limit ? offset ?", ChatRepository::mapChatMessage, limit, offset);
        Collections.reverse(list);
        history.setHistory(list);
        return history;
    }

    public void saveMessage(ChatMessage chatMessage){
        if(chatMessage.getId() == null){
            chatMessage.setId(template.queryForObject("select NEXT VALUE FOR SEQ_MESSAGE", Long.class));
            template.update("insert into chat_message(id, text, type, create_date, author, author_name, file_id) values (?, ?, ?, ?, ?, ?, ?)",
                    chatMessage.getId(),
                    chatMessage.getText(),
                    chatMessage.getType(),
                    chatMessage.getDate().toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT),
                    chatMessage.getAuthor(),
                    chatMessage.getAuthorName(),
                    chatMessage.getFileId());
        }else {
            template.update("update chat_message set text=?, type=?, create_date=?, author=?, author_name=?, file_id=? where id =?",
                    chatMessage.getText(),
                    chatMessage.getType(),
                    chatMessage.getDate().toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_INSTANT),
                    chatMessage.getAuthor(),
                    chatMessage.getAuthorName(),
                    chatMessage.getFileId(),
                    chatMessage.getId()
            );
        }
    }

    public ChatMessage getMessageById(Long id) {
        try {
            return template.queryForObject("select * from chat_message where id = ?", ChatRepository::mapChatMessage, id);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }
    }

    public ChatMessage getMessageByImageId(String fileId) {
        try {
            return template.queryForObject("select * from chat_message where file_id = ?", ChatRepository::mapChatMessage, fileId);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }
    }
}
