package client;

import common.message.codec.MessageCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.sound.sampled.*;

public class Client {

    /* STATICS */

    private static final Client INSTANCE = new Client();

    private static final AudioFormat TARGET_AUDIO_FORMAT = new AudioFormat(44100.0f,16,1,true,false);
    private static final AudioFormat SOURCE_AUDIO_FORMAT = new AudioFormat(44100.0f,16,2,true,false);
    private static final DataLine.Info TARGET_INFO = new DataLine.Info(TargetDataLine.class, TARGET_AUDIO_FORMAT);
    private static final DataLine.Info SOURCE_INFO = new DataLine.Info(SourceDataLine.class, SOURCE_AUDIO_FORMAT);

    public static Client getInstance() {
        return INSTANCE;
    }


    /* Instance Definition */

    // audio instances
    private TargetDataLine targetLine;
    private SourceDataLine sourceLine;

    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    private boolean audioStarted = false;
    private boolean channelOpened = false;

    public boolean isConnected() {
        return this.channelOpened;
    }

    public TargetDataLine getTargetLine() {
        return this.targetLine;
    }

    public SourceDataLine getSourceLine() {
        return this.sourceLine;
    }

    // bootstrap the client and claim audio resources
    public void start(String host, int port, int code) throws InterruptedException, LineUnavailableException {

        /* CLAIMING AUDIO DEVICES */
        if (!audioStarted) {
            System.out.println(1);
            this.targetLine = (TargetDataLine) AudioSystem.getLine(TARGET_INFO);
            this.targetLine.open();

            this.sourceLine = (SourceDataLine) AudioSystem.getLine(SOURCE_INFO);
            this.sourceLine.open();

            audioStarted = true;
        }

        // TODO : add multichannel audio

        /* BOOTSTRAPPING CLIENT */
        if (!channelOpened) {
            this.workerGroup = new NioEventLoopGroup();
            System.out.println(2);
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                public void initChannel(SocketChannel ch) {

                    ch.pipeline().addLast(
                            new MessageCodec(),
                            new ClientChannelHandler()
                    );

                }

            });
            System.out.println(3);

            this.channelFuture = b.connect(host, port).sync();
            System.out.println(4);
            channelOpened = true;
        }

        // TODO : remove
        ClientApplication.getApp().disableFields();
    }

    public void stop() {

        if (channelOpened) {

            this.targetLine.close();
            this.sourceLine.close();
            this.audioStarted = false;

            this.channelFuture.channel().closeFuture();
            this.workerGroup.shutdownGracefully();
            this.channelOpened = false;
        }
        System.out.println("Client channel has closed.");
        ClientApplication.getApp().updateStatus("Client process has ended.");
        // TODO : remove
        ClientApplication.getApp().enableFields();
    }

}
