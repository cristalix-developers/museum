package ru.cristalix.museum.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.cristalix.museum.packages.GreetingPackage;
import ru.cristalix.museum.packages.MuseumPackage;
import ru.cristalix.museum.utils.UtilNetty;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public class ClientSocket extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Class<? extends SocketChannel> CHANNEL_CLASS;
    private static final EventLoopGroup GROUP;

    private final Cache<String, CompletableFuture> responseCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();

    private Channel channel;

    private final String host;
    private final int port;
    private final String password;
    private final String serverName;

    private final Map<Class<? extends MuseumPackage>, Consumer> handlersMap = new HashMap<>();

    public void connect() {
        new Bootstrap()
                .channel(CHANNEL_CLASS)
                .group(GROUP)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        val config = ch.config();
                        config.setOption(ChannelOption.IP_TOS, 24);
                        config.setAllocator(PooledByteBufAllocator.DEFAULT);
                        config.setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                        config.setOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
                        ch.pipeline()
                                .addLast("codec", new HttpClientCodec())
                                .addLast("aggregator", new HttpObjectAggregator(65536))
                                .addLast("protocol_handler", new WebSocketClientProtocolHandler(
                                        WebSocketClientHandshakerFactory.newHandshaker(
                                                URI.create("http://" + host + ":" + port + "/"),
                                                WebSocketVersion.V13,
                                                null,
                                                false,
                                                new DefaultHttpHeaders(),
                                                65536
                                        ),
                                        true
                                ))
                                .addLast("handler_boss", this);
                    }
                })
                .remoteAddress(host, port)
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            System.out.println("Connection succeeded, bound to: " + (channel = future.channel()));
                        } else {
                            System.out.println("Connection failed");
                            future.cause().printStackTrace();
                            processAutoReconnect();
                        }
                    }
                });
    }

    private void sendHandshake() {
        GreetingPackage greetingPackage = new GreetingPackage(password, serverName);
        channel.writeAndFlush(UtilNetty.toFrame(greetingPackage)).addListeners(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE, new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("Handshake completed!");
                } else {
                    System.out.println("Error during handshake");
                    future.cause().printStackTrace();
                    future.channel().close();
                }
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            sendHandshake();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            MuseumPackage pckg = UtilNetty.readFrame(frame);
            CompletableFuture future = responseCache.getIfPresent(pckg.getId());
            if (future != null) {
                responseCache.invalidate(pckg.getId());
                future.complete(pckg);
            }
            Consumer consumer = handlersMap.get(pckg.getClass());
            if (consumer != null)
                consumer.accept(pckg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channel.close();
        channel = null;
        responseCache.invalidateAll();
        processAutoReconnect();
    }

    public <T extends MuseumPackage> CompletableFuture<T> writeAndAwaitResponse(T pckg) {
        val future = awaitResponse(pckg);
        write(pckg);
        return future;
    }

    public <T extends MuseumPackage> CompletableFuture<T> awaitResponse(T pckg) {
        CompletableFuture<T> future = new CompletableFuture<>();
        responseCache.put(pckg.getId(), future);
        return future;
    }

    public void write(MuseumPackage museumPackage) {
        write(UtilNetty.toFrame(museumPackage));
    }

    private void write(TextWebSocketFrame frame) {
        channel.writeAndFlush(frame, channel.voidPromise());
    }

    public <T extends MuseumPackage> void registerHandler(Class<T> clazz, Consumer<T> consumer) {
        handlersMap.put(clazz, consumer);
    }

    private void processAutoReconnect() {
        System.out.println("Automatically reconnecting in next 1.5 seconds");
        schedule(this::connect, 1500L, TimeUnit.MILLISECONDS);
    }

    public void schedule(Runnable command, long delay, TimeUnit unit) {
        GROUP.schedule(command, delay, unit);
    }

    static {
        boolean epoll;
        try {
            Class.forName("io.netty.channel.epoll.Epoll");
            epoll = !Boolean.getBoolean("cristalix.net.disable-native-transport") && Epoll.isAvailable();
        } catch (ClassNotFoundException ignored) {
            epoll = false;
        }
        if (epoll) {
            CHANNEL_CLASS = EpollSocketChannel.class;
            GROUP = new EpollEventLoopGroup(1);
        } else {
            CHANNEL_CLASS = NioSocketChannel.class;
            GROUP = new NioEventLoopGroup(1);
        }
    }

}
