package museum.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import museum.MuseumService;
import museum.packages.GreetingPackage;
import museum.packages.MuseumPackage;
import museum.realm.Realm;
import museum.utils.UtilNetty;
import ru.cristalix.core.realm.RealmId;

import java.net.InetSocketAddress;
import java.util.Optional;

@RequiredArgsConstructor
public class ServerSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final AttributeKey<Realm> realmKey = AttributeKey.newInstance("realm");

	private final ServerSocket serverSocket;

	@Override
	@SuppressWarnings ("unchecked")
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {

		if (!(msg instanceof TextWebSocketFrame)) return;

		MuseumPackage museumPackage = UtilNetty.readFrame((TextWebSocketFrame) msg);
		Channel channel = ctx.channel();
		if (museumPackage instanceof GreetingPackage) {
			if (channel.hasAttr(realmKey)) {
				System.out.println("Some channel tries to authorize, but it already in system!");
				return;
			}
			GreetingPackage packet = (GreetingPackage) museumPackage;
			RealmId realmId = RealmId.of(packet.getServerName());

			if (serverSocket.getConnectedChannels().containsKey(realmId)) {
				System.out.println("Channel want to register as " + packet.getServerName() + ", but this name already in use!");
				ctx.close();
				return;
			}
			if (!packet.getPassword().equals(MuseumService.getInstance().getPassword())) {
				System.out.println("Channel provided bad password: " + packet.getPassword());
				if (channel.remoteAddress() instanceof InetSocketAddress) {
					System.out.println(channel.remoteAddress().toString());
				}
				ctx.close();
				return;
			}
			Realm realm = new Realm(serverSocket, channel, realmId);

			channel.attr(realmKey).set(realm);

			serverSocket.getConnectedChannels().put(realm.getId(), channel);
			realm.send(MuseumService.getInstance().getConfigurationManager().pckg());
			System.out.println("Server authorized! " + packet.getServerName());
		} else {
			if (!channel.hasAttr(realmKey)) {
				System.out.println("Some channel tries to send packet without authorization!");
				if (channel.remoteAddress() instanceof InetSocketAddress) {
					System.out.println(channel.remoteAddress().toString());
				}
				ctx.close();
				return;
			}
			Realm realm = channel.attr(realmKey).get();
			Optional.ofNullable(MuseumService.getInstance().getHandlerMap().get(museumPackage.getClass()))
					.ifPresent(handler -> handler.handle(realm, museumPackage));
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		if (channel.hasAttr(realmKey)) {
			Realm realm = channel.attr(realmKey).get();
			serverSocket.getConnectedChannels().remove(realm.getId());
			System.out.println("Server disconnected! " + realm);
		}
	}

}
