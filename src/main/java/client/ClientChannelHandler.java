package client;

import common.message.audio.AudioMessage;
import common.message.connection.CodeMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientChannelHandler extends ChannelInboundHandlerAdapter {

    private static final UUID NIL_UUID = new UUID(0,0);
    private UUID uuid = NIL_UUID;

    private final int MAX_AUDIO_QUEUE_SIZE = 50;
    private final ConcurrentLinkedQueue<AudioMessage> AUDIO_MESSAGES_QUEUE = new ConcurrentLinkedQueue<>();

    private final AudioMessage OUTBOUND_AUDIO_MESSAGE = new AudioMessage();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        /* SEND CONNECTION CODE TO THE SERVER */
        CodeMessage outboundCodeMessage = new CodeMessage();
        outboundCodeMessage.setCode(ClientApplication.code);

        ctx.writeAndFlush(outboundCodeMessage);

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

                if (line.available() >= buffer.length) {

                    line.read(buffer, 0, buffer.length);
                    Thread channelWriteThread = new Thread(()->ctx.writeAndFlush(OUTBOUND_AUDIO_MESSAGE));
                    channelWriteThread.start();

                }

            }
            System.out.println("Capture line ended.");
        });
        outboundThread.start();

        /* PLAYBACK FROM QUEUE THREAD */

        Thread inboundThread = new Thread(()->{

            SourceDataLine line = Client.getInstance().getSourceLine();

            while (ctx.channel().isOpen() && line.isOpen()) {

                AudioMessage inboundAudioMessage = AUDIO_MESSAGES_QUEUE.poll();

                if (inboundAudioMessage != null) {

                    float gain = inboundAudioMessage.getGain();
                    float pan = inboundAudioMessage.getPan();
                    byte[] sound = inboundAudioMessage.getSound();

                    byte[] stereoSound = new byte[sound.length * 2];

                    for (int i = 0; i < sound.length; i += 2) {
                        stereoSound[i * 2] = sound[i];
                        stereoSound[i*2+1] = sound[i+1];
                        stereoSound[i*2+2] = sound[i];
                        stereoSound[i*2+3] = sound[i+1];
                    }

                    FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                    FloatControl panControl = (FloatControl) line.getControl(FloatControl.Type.PAN);

                    gainControl.setValue(gain);
                    panControl.setValue(pan);

                    line.start();
                    line.write(stereoSound, 0, stereoSound.length);

                } else {
                    line.drain();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        });
        inboundThread.start();

        ClientApplication.getApp().disableFields();

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Client.getInstance().stop();
        ClientApplication.getApp().enableFields();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof CodeMessage) {

            CodeMessage message = (CodeMessage) msg;

            if (uuid == NIL_UUID) {

                uuid = message.getUUID();
                ClientApplication.getApp().updateStatus("Obtained UUID " + uuid);

            } else {
                ClientApplication.getApp().updateStatus("Nil returned, unsuccessful request");
            }

        } else if (msg instanceof AudioMessage) {

            if (AUDIO_MESSAGES_QUEUE.size() <= MAX_AUDIO_QUEUE_SIZE) {
                AUDIO_MESSAGES_QUEUE.add((AudioMessage) msg);
            }

        } else {
            System.out.println("Unexpected message received.");
            ClientApplication.getApp().updateStatus("Unexpected message received, closing connection...");
            ctx.fireChannelInactive();
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
