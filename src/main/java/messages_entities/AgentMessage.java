package messages_entities;

public class AgentMessage {
    private long id;
    private String message;

    public AgentMessage(String message, long id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public long getId() {
        return id;
    }
}
