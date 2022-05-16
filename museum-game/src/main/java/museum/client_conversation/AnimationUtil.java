package museum.client_conversation;

import lombok.val;
import me.func.mod.Anime;
import me.func.mod.conversation.ModTransfer;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import ru.cristalix.core.realm.IRealmService;

import java.text.DecimalFormat;

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

	public static void updateMoney(User user) {
		new ModTransfer()
				.string(MessageUtil.toMoneyFormat(user.getMoney()))
				.send("museum:balance", user.handle());
	}

	public static void updateOnlineAll() {
		val transfer = new ModTransfer().integer(IRealmService.get().getOnlineOnRealms("MUSM"));
		Bukkit.getOnlinePlayers().forEach(player -> transfer.send("museum:online", player));
	}

	public static void updateIncome(User user) {
		new ModTransfer()
				.string(new DecimalFormat("#00.0").format(user.getIncome()))
				.send("museum:coinprice", user.handle());
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
		user.closeInventory();
	}
}
