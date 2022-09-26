package museum.player.prepare;

import clepto.bukkit.B;
import me.func.mod.conversation.ModLoader;
import me.func.mod.ui.scoreboard.ScoreBoard;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.player.User;
import museum.util.MessageUtil;
import ru.cristalix.core.realm.IRealmService;

import java.text.DecimalFormat;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareMods implements Prepare {

	public static final Prepare INSTANCE = new PrepareMods();

	static {
		DecimalFormat BALANCE_FORMAT = new DecimalFormat("###,###,###,###,###,###");

		ScoreBoard.builder()
				.key("scoreboard")
				.header("Музей")
				.dynamic("Баланс", player -> "§a" + BALANCE_FORMAT.format(App.getApp().getUser(player).getMoney()) + " \uE03F")
				.dynamic("Доход в 5 сек.", player -> "§e" + MessageUtil.toMoneyFormat(App.getApp().getUser(player).getIncome()))
				.dynamic("Опыт", player -> "§b" + App.getApp().getUser(player).getExperience())
				.dynamic("Онлайн", player -> IRealmService.get().getOnlineOnRealms("MUSM"))
				.build();
	}

	@Override
	public void execute(User user, App app) {
		ModLoader.send("museum-mod-bundle.jar", user.getPlayer());
		B.postpone(30, () -> {
			AnimationUtil.updateLevelBar(user);
			ScoreBoard.subscribe("scoreboard", user.handle());
		});
	}
}