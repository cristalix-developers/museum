package museum.player.prepare;

import lombok.val;
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
	private static final SubjectPrototype.SubjectDataForClient[] SHOP_DATA = Managers.subject.getMap().values().stream().map(SubjectPrototype::getDataForClient).toArray(SubjectPrototype.SubjectDataForClient[]::new);

	private ModTransfer dataForClients;
	private long lastUpdate = 0;

	@Override
	public void execute(User user, App app) {
		val now = System.currentTimeMillis();
		// Обновление позиций в магазине каждую минуту
		if (dataForClients == null || now - lastUpdate > 1000 * 60) {
			lastUpdate = now;
			dataForClients = new ModTransfer().json(SHOP_DATA);
		}
		dataForClients.send("shop", user.handle());
	}
}