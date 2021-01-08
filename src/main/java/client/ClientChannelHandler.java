package client;

import common.message.audio.AudioMessage;
import common.message.connection.CodeMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.sound.sampled.TargetDataLine;
import java.util.HashMap;
import java.util.UUID;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final UUID NIL_UUID = new UUID(0,0);
    private static UUID uuid = NIL_UUID;

    private final HashMap<UUID, AudioChannel> AUDIO_CHANNELS = Client.getInstance().getAudioChannels();

    private final AudioMessage OUTBOUND_AUDIO_MESSAGE = new AudioMessage();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        /* SEND CONNECTION CODE TO THE SERVER */
        CodeMessage outboundCodeMessage = new CodeMessage();
        outboundCodeMessage.setCode(ClientApplication.code);

        System.out.println("Sending CodeMessage " + outboundCodeMessage.getCode());

        ctx.writeAndFlush(outboundCodeMessage);

        ClientApplication.getApp().disableFields();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Client.getInstance().stop();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof CodeMessage) {

            CodeMessage message = (CodeMessage) msg;

            if (uuid.equals(NIL_UUID)) { // If UUID has not been updated (therefore not authenticated)

                if (!message.getUUID().equals(NIL_UUID)) { // If received UUID isn't NIL_UUID (therefore denied code)

                    /* Updating UUID to Minecraft Player UUID */

                    uuid = message.getUUID();
                    ClientApplication.getApp().updateStatus("Obtained UUID " + uuid);
                    System.out.println("Received UUID " + uuid);


                    /* CAPTURE AND SEND THREAD */

                    Thread outboundThread = new Thread(()->{

                        TargetDataLine line = Client.getInstance().getTargetLine();
                        if (line.isOpen()) {
                            line.start();
                        } else {
                            System.out.println("Capturing line is not open.");
                            return;
                        }

                        byte[] buffer = new byte[AudioMessage.SOUND_SIZE];

                        System.out.println("Capture line started.");

                        OUTBOUND_AUDIO_MESSAGE.setSound(buffer);
                        OUTBOUND_AUDIO_MESSAGE.setUUID(uuid);

                        while (ctx.channel().isOpen() && line.isOpen()) {

                            if (line.available() >= buffer.length && uuid != NIL_UUID) {

                                line.read(buffer, 0, buffer.length);
                                ctx.writeAndFlush(OUTBOUND_AUDIO_MESSAGE);

                            }

                        }
                        System.out.println("Capture line ended.");
                    });
                    outboundThread.start();

                } else {
                    ClientApplication.getApp().updateStatus("Nil returned, unsuccessful request");
                }

            } else {
                System.out.println("Faulty Connection Message received.");
            }

        } else if (msg instanceof AudioMessage) {

            AudioMessage message = (AudioMessage) msg;

            UUID messageUUID = message.getUUID();

            if (messageUUID.equals(NIL_UUID)) return;

            AudioChannel channel = AUDIO_CHANNELS.get(messageUUID);
            if (channel != null) {
                channel.enqueue(message);
            } else {
                AUDIO_CHANNELS.put(
                        messageUUID,
                        new AudioChannel(messageUUID)
                );
            }

        } else {
            System.out.println("Unexpected message received.");
            ClientApplication.getApp().updateStatus("Unexpected message received, closing connection...");
            ctx.fireChannelInactive();
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}
