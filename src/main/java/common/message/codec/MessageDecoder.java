package common.message.codec;

import io.netty.handler.codec.serialization.ClassResolver;
import io.netty.handler.codec.serialization.ObjectDecoder;

public class MessageDecoder extends ObjectDecoder {

    public MessageDecoder(ClassResolver classResolver) {
        super(classResolver);
    }

}
