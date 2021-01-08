package common.message.audio;

import common.message.AbstractMessage;

import java.nio.ByteBuffer;

public class AudioMessage extends AbstractMessage {

    public static final byte TYPE_CODE = 2;
    public static final int SOUND_SIZE = 2048;
    public static final int LENGTH = AbstractMessage.LENGTH + Float.BYTES + Float.BYTES + SOUND_SIZE;

    private float gain = 0;
    private float pan = 0;
    private byte[] sound = new byte[SOUND_SIZE];

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getPan() {
        return pan;
    }

    public void setPan(float pan) {
        this.pan = pan;
    }

    public byte[] getSound() {
        return sound;
    }

    public void setSound(byte[] sound) {
        this.sound = sound;
    }

    public void copySound(byte[] sound) {
        ByteBuffer.wrap(sound).get(this.sound);
    }

    @Override
    protected int getLength() {
        return LENGTH;
    }

    @Override
    protected byte getTypeCode() {
        return TYPE_CODE;
    }

    @Override
    protected ByteBuffer contentToBytes(ByteBuffer buf) {
        return buf.putFloat(gain).putFloat(pan).put(sound);
    }

    @Override
    protected ByteBuffer contentFromBytes(ByteBuffer buf) {
        gain = buf.getFloat();
        pan = buf.getFloat();
        return buf.get(sound);
    }

}
