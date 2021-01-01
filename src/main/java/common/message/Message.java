package common.message;

import java.io.Serializable;
import java.util.UUID;

@Deprecated
public abstract class Message<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected UUID uuid;
    T content;

    protected Message(UUID uuid, T content) {
        this.uuid = uuid;
        this.content = content;
    }

    public UUID getUUID() {
        return this.uuid;
    }
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    protected T getContent() {
        return this.content;
    }
    protected void setContent(T content) {
        this.content = content;
    }

}