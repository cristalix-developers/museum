package museum.service.conduct;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import museum.packages.MuseumPackage;
import museum.service.conduct.socket.ServerSocket;
import museum.utils.UtilNetty;
import ru.cristalix.core.realm.RealmId;

@AllArgsConstructor
@Data
public class Realm {

	private final ServerSocket serverSocket;
	private Channel channel;
	private RealmId id;

	public void send(TextWebSocketFrame frame) {
		channel.write(frame, channel.voidPromise());
	}

	public void send(MuseumPackage museumPackage) {
		send(UtilNetty.toFrame(museumPackage));
	}

	@Override
	public String toString() {
		return id.getRealmName();
	}

}
