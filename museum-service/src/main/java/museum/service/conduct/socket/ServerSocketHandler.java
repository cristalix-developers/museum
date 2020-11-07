package museum.service.conduct.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import museum.packages.GreetingPackage;
import museum.packages.MuseumPackage;
import museum.service.MuseumService;
import museum.service.conduct.IConductService;
import museum.service.conduct.Realm;
import museum.utils.UtilNetty;
import ru.cristalix.core.realm.RealmId;

import java.net.InetSocketAddress;

@Slf4j
@RequiredArgsConstructor
public class ServerSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	private static final AttributeKey<Realm> realmKey = AttributeKey.newInstance("realm");

	private final ServerSocket serverSocket;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {

		if (!(msg instanceof TextWebSocketFrame)) return;

		IConductService conductService = serverSocket.getConductService();

		MuseumPackage museumPackage = UtilNetty.readFrame((TextWebSocketFrame) msg);
		Channel channel = ctx.channel();
		if (museumPackage instanceof GreetingPackage) {
			if (channel.hasAttr(realmKey)) {
				log.warn("Some channel tries to authorize, but it already in system!");
				return;
			}
			GreetingPackage packet = (GreetingPackage) museumPackage;
			RealmId realmId = RealmId.of(packet.getServerName());

			if (conductService.getRealm(realmId) != null) {
				log.warn("Channel wants to register as " + packet.getServerName() + ", but this name already in use!");
				ctx.close();
				return;
			}
			if (!packet.getPassword().equals(MuseumService.getInstance().getPassword())) {
				log.warn("Channel provided bad password: " + packet.getPassword());
				if (channel.remoteAddress() instanceof InetSocketAddress) {
					log.warn(channel.remoteAddress().toString());
				}
				ctx.close();
				return;
			}
			Realm realm = new Realm(serverSocket, channel, realmId);

			channel.attr(realmKey).set(realm);

			conductService.registerRealm(realm);

			realm.send(MuseumService.getInstance().getConfigService().createBundle());
			log.info("Server authorized! " + packet.getServerName());
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

			conductService.handlePacket(realm, museumPackage);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		if (channel.hasAttr(realmKey)) {
			Realm realm = channel.attr(realmKey).get();
			serverSocket.getConductService().unregisterRealm(realm);
			System.out.println("Server disconnected! " + realm);
		}
	}

}
