package common.message;

import java.nio.ByteBuffer;
import java.util.UUID;

public abstract class AbstractMessage {

    protected UUID uuid = new UUID(0,0);
    public static final int LENGTH = Byte.BYTES + Long.BYTES + Long.BYTES;

    protected abstract int getLength();
    protected abstract byte getTypeCode();

    public final UUID getUUID() {
        return this.uuid;
    }

    public final void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public final void toBytes(byte[] bytes) {
        ByteBuffer buf = contentToBytes(
                ByteBuffer.wrap(bytes)
                .put(getTypeCode())
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
        );
        buf.rewind();
        buf.get(bytes);
    }

    protected abstract ByteBuffer contentToBytes(ByteBuffer buf);

    public final void fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        if (buf.get() == getTypeCode()) {
            uuid = new UUID(buf.getLong(), buf.getLong());
            contentFromBytes(buf);
        }
    }

    protected abstract ByteBuffer contentFromBytes(ByteBuffer buf);

}
