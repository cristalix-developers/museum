package museum.player.prepare;

import museum.App;
import museum.client_conversation.ClientPacket;
import museum.player.User;

/**
 * @author func 04.10.2020
 * @project museum
 */
public class PrepareTop implements Prepare {
	public static final Prepare INSTANCE = new PrepareTop();

	private final ClientPacket packet = new ClientPacket("top-create");

	@Override
	public void execute(User user, App app) {
		packet.send(user, "{\"INCOME\":{\"x\":266,\"y\":89.8,\"z\":-270,\"yaw\":-135,\"title\":\"Топ по доходу\"},\"EXPERIENCE\":{\"x\":261,\"y\":89.8,\"z\":-278,\"yaw\":-90,\"title\":\"Топ по опыту\"},\"MONEY\":{\"x\":266,\"y\":90,\"z\":-286,\"yaw\":-45,\"title\":\"Топ по деньгам\"}}");
	}
}
