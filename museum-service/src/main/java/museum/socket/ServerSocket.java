package museum.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServerSocket extends Thread {

	private static final Class<? extends ServerSocketChannel> CHANNEL_CLASS;
	private static final EventLoopGroup BOSS_GROUP, WORKER_GROUP;

	static {
		boolean epoll;
		try {
			Class.forName("io.netty.channel.epoll.Epoll");
			epoll = !Boolean.getBoolean("cristalix.net.disable-native-transport") && Epoll.isAvailable();
		} catch (ClassNotFoundException e) {
			epoll = false;
		}
		if (epoll) {
			CHANNEL_CLASS = EpollServerSocketChannel.class;
			BOSS_GROUP = new EpollEventLoopGroup(1);
			WORKER_GROUP = new EpollEventLoopGroup(1);
		} else {
			CHANNEL_CLASS = NioServerSocketChannel.class;
			BOSS_GROUP = new NioEventLoopGroup(1);
			WORKER_GROUP = new NioEventLoopGroup(1);
		}
	}

	private final int port;

	@Override
	public void run() {
		try {
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap
					.group(BOSS_GROUP, WORKER_GROUP)
					.channel(CHANNEL_CLASS)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) {
							socketChannel.pipeline().addLast(
									new HttpRequestDecoder(),
									new HttpObjectAggregator(6553600),
									new HttpResponseEncoder(),
									new WebSocketServerProtocolHandler("/", null, false, 6553600),
									new ServerSocketHandler()
							);
						}
					});

			serverBootstrap.bind(port).sync().channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			BOSS_GROUP.shutdownGracefully();
			WORKER_GROUP.shutdownGracefully();
		}
	}
}
