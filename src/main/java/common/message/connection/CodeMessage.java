package common.message.connection;

import common.message.AbstractMessage;

import java.nio.ByteBuffer;

public class CodeMessage extends AbstractMessage {

    public static final byte TYPE_CODE = 1;
    public static final int LENGTH = AbstractMessage.LENGTH + Integer.BYTES;

    private int code;

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
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
        return buf.putInt(code);
    }

    @Override
    protected ByteBuffer contentFromBytes(ByteBuffer buf) {
        code = buf.getInt();
        return buf;
    }

}
