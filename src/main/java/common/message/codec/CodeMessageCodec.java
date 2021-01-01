package common.message.codec;

import common.message.connection.CodeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class CodeMessageCodec extends ByteToMessageCodec<CodeMessage> {
    byte[] writeBuffer = new byte[CodeMessage.LENGTH];
    byte[] readBuffer = new byte[CodeMessage.LENGTH];

    @Override
    protected void encode(ChannelHandlerContext ctx, CodeMessage msg, ByteBuf buf) throws Exception {
        msg.toBytes(writeBuffer);
        buf.writeBytes(writeBuffer);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        if (buf.readableBytes() >= CodeMessage.LENGTH) {
            if (buf.getByte(buf.readerIndex()) == CodeMessage.TYPE_CODE) {
                buf.readBytes(readBuffer);
                CodeMessage msg = new CodeMessage();
                msg.fromBytes(readBuffer);
                list.add(msg);
            }
        }
    }
}
