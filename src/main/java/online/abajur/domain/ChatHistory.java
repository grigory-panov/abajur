package online.abajur.domain;

import java.util.List;

/**
 * Created by gr on 21.04.18.
 */
public class ChatHistory {
    private int totalMessages;
    private List<ChatMessage> history;

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public List<ChatMessage> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessage> history) {
        this.history = history;
    }
}
