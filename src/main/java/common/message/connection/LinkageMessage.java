package common.message.connection;

import common.message.Message;

import java.util.UUID;

@Deprecated
public class LinkageMessage extends Message<Integer> {

    private static final long serialVersionUID = 1L;

    public LinkageMessage(UUID uuid, Integer content) {
        super(uuid, content);
    }

    public int getCode() {
        return getContent();
    }

    public void setCode(int code) {
        setContent(code);
    }

}
