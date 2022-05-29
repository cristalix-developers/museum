package museum.player.prepare;

import clepto.bukkit.B;
import me.func.mod.conversation.ModLoader;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.player.User;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareMods implements Prepare {

	public static final Prepare INSTANCE = new PrepareMods();

	@Override
	public void execute(User user, App app) {
		ModLoader.send("museum-mod-bundle.jar", user.getPlayer());
		B.postpone(30, () -> {
			AnimationUtil.updateLevelBar(user);
			AnimationUtil.updateMoney(user);
			AnimationUtil.updateOnlineAll();
		});
	}
}