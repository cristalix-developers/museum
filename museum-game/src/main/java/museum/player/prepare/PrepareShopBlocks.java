package museum.player.prepare;

import me.func.mod.conversation.ModTransfer;
import museum.App;
import museum.museum.map.SubjectPrototype;
import museum.player.User;
import museum.prototype.Managers;

/**
 * @author func 04.10.2020
 * @project museum
 */
public class PrepareShopBlocks implements Prepare {

	public static final Prepare INSTANCE = new PrepareShopBlocks();

	private ModTransfer dataForClients;

	@Override
	public void execute(User user, App app) {
		if (dataForClients == null) {
			dataForClients = new ModTransfer().json(
					Managers.subject.getMap().values().stream()
							.map(SubjectPrototype::getDataForClient)
							.toArray(SubjectPrototype.SubjectDataForClient[]::new)
			);
		}
		dataForClients.send("shop", user.handle());
	}
}