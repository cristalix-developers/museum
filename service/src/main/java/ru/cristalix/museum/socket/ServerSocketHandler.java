package ru.cristalix.museum.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.MuseumService;
import ru.cristalix.museum.packages.GreetingPackage;
import ru.cristalix.museum.packages.MuseumPackage;
import ru.cristalix.museum.utils.UtilNetty;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServerSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final AttributeKey<String> serverInfoKey = AttributeKey.newInstance("serverinfo");

	private static final Map<String, Channel> connectedChannels = new ConcurrentHashMap<>();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
		if (msg instanceof TextWebSocketFrame) {
			MuseumPackage museumPackage = UtilNetty.readFrame((TextWebSocketFrame) msg);
			Channel channel = ctx.channel();
			if (museumPackage instanceof GreetingPackage) {
				if (channel.hasAttr(serverInfoKey)) {
					System.out.println("Some channel tries to authorize, but it already in system!");
					return;
				}
				GreetingPackage pckg = (GreetingPackage) museumPackage;
				if (connectedChannels.containsKey(pckg.getServerName())) {
					System.out.println("Channel want to register as " + pckg.getServerName() + ", but this name already in use!");
					ctx.close();
					return;
				}
				if (!pckg.getPassword().equals(MuseumService.PASSWORD)) {
					System.out.println("Channel provided bad password: " + pckg.getPassword());
					if (channel.remoteAddress() instanceof InetSocketAddress) {
						System.out.println(channel.remoteAddress().toString());
					}
					ctx.close();
					return;
				}
				channel.attr(serverInfoKey).set(pckg.getServerName());
				connectedChannels.put(pckg.getServerName(), channel);
				CoreApi.get().getPlatform().getScheduler().runAsyncDelayed(() -> {
					send(channel, MuseumService.SQL_MANAGER.pckg());
					send(channel, MuseumService.CONFIGURATION_MANAGER.pckg());
				}, 1L, TimeUnit.SECONDS);
				System.out.println("Server authorized! " + pckg.getServerName());
			} else {
				if (!channel.hasAttr(serverInfoKey)) {
					System.out.println("Some channel tries to send packet without authorization!");
					if (channel.remoteAddress() instanceof InetSocketAddress) {
						System.out.println(channel.remoteAddress().toString());
					}
					ctx.close();
					return;
				}
				String info = channel.attr(serverInfoKey).get();
				Optional.ofNullable(MuseumService.HANDLER_MAP.get(museumPackage.getClass())).ifPresent(handler -> handler.handle(channel, info, museumPackage));
			}
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		if (channel.hasAttr(serverInfoKey)) {
			String name = channel.attr(serverInfoKey).get();
			connectedChannels.remove(name);
			System.out.println("Server disconnected! " + name);
		}
	}

	public static void broadcast(MuseumPackage pckg) {
		broadcast(UtilNetty.toFrame(pckg));
	}

	public static void broadcast(TextWebSocketFrame pckg) {
		connectedChannels.values().forEach(channel -> send(channel, pckg));
	}

	public static void send(Channel channel, MuseumPackage pckg) {
		send(channel, UtilNetty.toFrame(pckg));
	}

	public static void send(Channel channel, TextWebSocketFrame frame) {
		channel.writeAndFlush(frame, channel.voidPromise());
	}

}
