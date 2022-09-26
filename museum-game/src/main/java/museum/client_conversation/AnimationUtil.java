package museum.client_conversation;

import lombok.val;
import me.func.mod.Anime;
import me.func.mod.conversation.ModTransfer;
import me.func.mod.ui.Glow;
import me.func.protocol.data.color.GlowColor;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.LevelSystem;

/**
 * @author func 02.01.2021
 * @project museum
 */
public class AnimationUtil {

	public static void generateMessage(String message, String channel, User user) {
		new ModTransfer()
				.string(message)
				.send(channel, user.handle());
	}

	public static void updateLevelBar(User user) {
		val level = user.getLevel();
		val beforeExperience = LevelSystem.getRequiredExperience(level - 1);
		new ModTransfer()
				.integer(level)
				.integer((int) (user.getExperience() - beforeExperience))
				.integer((int) (LevelSystem.getRequiredExperience(level) - beforeExperience))
				.send("museum:levelbar", user.handle());
	}

	public static void updateCosmoCrystal(User user) {
		new ModTransfer()
				.integer(user.getCosmoCrystal())
				.send("museum:cosmo-crystal", user.handle());
	}

	public static void leaveCosmos(User user) {
		new ModTransfer()
				.integer(user.getCosmoCrystal())
				.send("museum:cosmo-leave", user.handle());
	}

	public static void buyFailure(User user) {
		Anime.itemTitle(user.handle(), BeforePacketHandler.EMERGENCY_STOP, "Ошибка", "Недостаточно средств", 3.0);
		Glow.animate(user.getPlayer(), 2.0, GlowColor.RED);
		user.closeInventory();
	}
}
