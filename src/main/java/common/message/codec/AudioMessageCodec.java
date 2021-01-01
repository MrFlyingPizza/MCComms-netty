package common.message.codec;

import common.message.audio.AudioMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class AudioMessageCodec extends ByteToMessageCodec<AudioMessage> {

    byte[] writeBuffer = new byte[AudioMessage.LENGTH];
    byte[] readBuffer = new byte[AudioMessage.LENGTH];

    @Override
    protected void encode(ChannelHandlerContext ctx, AudioMessage msg, ByteBuf buf) throws Exception {
        msg.toBytes(writeBuffer);
        buf.writeBytes(writeBuffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (buf.readableBytes() >= AudioMessage.LENGTH) {
            if (buf.getByte(buf.readerIndex()) == AudioMessage.TYPE_CODE) {
                buf.readBytes(readBuffer);
                AudioMessage msg = new AudioMessage();
                msg.fromBytes(readBuffer);
                list.add(msg);
            }
        }
    }
}
