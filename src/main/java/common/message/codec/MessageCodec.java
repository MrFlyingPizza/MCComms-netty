package common.message.codec;

import common.message.AbstractMessage;
import common.message.audio.AudioMessage;
import common.message.connection.CodeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class MessageCodec extends ByteToMessageCodec<AbstractMessage> {

    byte[] writeCodeBuffer = new byte[CodeMessage.LENGTH];
    byte[] readCodeBuffer = new byte[CodeMessage.LENGTH];

    byte[] writeAudioBuffer = new byte[AudioMessage.LENGTH];
    byte[] readAudioBuffer = new byte[AudioMessage.LENGTH];

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractMessage msg, ByteBuf buf) throws Exception {

        if (msg instanceof CodeMessage) {
            CodeMessage message = (CodeMessage) msg;
            message.toBytes(writeCodeBuffer);
            buf.writeBytes(writeCodeBuffer);
        } else if (msg instanceof AudioMessage) {
            AudioMessage message = (AudioMessage) msg;
            message.toBytes(writeAudioBuffer);
            buf.writeBytes(writeAudioBuffer);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {

        if (buf.readableBytes() >= CodeMessage.LENGTH) {
            if (buf.getByte(buf.readerIndex()) == CodeMessage.TYPE_CODE) {
                buf.readBytes(readCodeBuffer);
                CodeMessage msg = new CodeMessage();
                msg.fromBytes(readCodeBuffer);
                list.add(msg);
            }
        }
        if (buf.readableBytes() >= AudioMessage.LENGTH) {
            if (buf.getByte(buf.readerIndex()) == AudioMessage.TYPE_CODE) {
                buf.readBytes(readAudioBuffer);
                AudioMessage msg = new AudioMessage();
                msg.fromBytes(readAudioBuffer);
                list.add(msg);
            }
        }
    }
}
