package server;

import common.message.codec.MessageDecoder;
import common.message.codec.MessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.UUID;
import java.util.concurrent.*;

public class Server {

    private static final ConcurrentHashMap<Integer, UUID> PENDING_CODES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Connection> CONNECTIONS = new ConcurrentHashMap<>();

    public static double VC_DIST_SQUARED = 40 * 40;

    private static final Server INSTANCE = new Server();

    public static Server getInstance() {
        return INSTANCE;
    }

    public static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    public static ScheduledExecutorService getExecutorService() {
        return EXECUTOR_SERVICE;
    }

    public static ConcurrentMap<Integer, UUID> getPending() {
        return PENDING_CODES;
    }
    public static ConcurrentHashMap<UUID, Connection> getConnections() {
        return CONNECTIONS;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public void start(int port) throws InterruptedException {

        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {

                        ch.pipeline().addLast(
                                new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(Server.class.getClassLoader())),
                                new ServerChannelHandler()
                        );

                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        this.channelFuture = b.bind(port).sync();
        System.out.println("Voice client server started.");
    }

    public void stop() {
        this.channelFuture.channel().closeFuture();
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}
