package museum.player.prepare;

import museum.App;
import museum.client_conversation.ModTransfer;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.prototype.Managers;
import ru.cristalix.core.GlobalSerializers;

/**
 * @author func 04.10.2020
 * @project museum
 */
public class PrepareShopBlocks implements Prepare {

	public static final Prepare INSTANCE = new PrepareShopBlocks();

	private String dataForClients;

	@Override
	public void execute(User user, App app) {
		if (dataForClients == null || dataForClients.isEmpty()) {
			dataForClients = GlobalSerializers.toJson(
					Managers.subject.getMap().values().stream()
							.map(SubjectPrototype::getDataForClient)
							.toArray(SubjectPrototype.SubjectDataForClient[]::new)
			);
		}
		new ModTransfer().string(dataForClients).send("shop", user);
	}
}