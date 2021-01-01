package common.message.audio;

import common.message.Message;

import java.util.UUID;

@Deprecated
public class PlainAudioMessage extends Message<byte[]> {

    private static final long serialVersionUID = 1L;

    public PlainAudioMessage(UUID uuid, byte[] content) {
        super(uuid, content);
    }

    public byte[] getSound() {
        return getContent();
    }

    public void setSound(byte[] sound) {
        setContent(sound);
    }

}
