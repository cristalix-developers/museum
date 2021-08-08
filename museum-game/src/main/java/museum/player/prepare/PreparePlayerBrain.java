package museum.player.prepare;

import clepto.bukkit.B;
import clepto.bukkit.Cycle;
import clepto.bukkit.menu.Guis;
import clepto.bukkit.world.Label;
import com.destroystokyo.paper.Title;
import implario.ListUtils;
import lombok.val;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.client_conversation.ModTransfer;
import museum.fragment.Gem;
import museum.fragment.GemType;
import museum.museum.Museum;
import museum.player.User;
import museum.util.LocationUtil;
import museum.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static clepto.bukkit.Cycle.exit;

/**
 * @author func 26.08.2020
 * @project museum
 */
public class PreparePlayerBrain implements Prepare {

	public static final Prepare INSTANCE = new PreparePlayerBrain();
	public static final int EXPERIENCE = 3;
	public static final long REWARD_DELAY_HOURS = 18;

	private final List<Label> dots;
	private final List<Title> titles = new ArrayList<>();

	public PreparePlayerBrain() {
		dots = App.getApp().getMap().getLabels("guide");

		// При next след. текст становится на второе место
		Stream.of(
				"Привет! 䀈", "Это.nextТвой. Музей. 㸾", "Заполняйnextвитрины 㜤",
				"Раскапывайnextдинозавров 㿿", "Находи секреты 㜰", "Кастомизируй 㟡",
				"Играй сnextдрузьями 㭿", "Удачи! 㲺"
		).map(line -> {
			if (line.contains("next")) {
				String[] separated = line.split("next");
				return new Title(separated[0], separated[1]);
			}
			return new Title(line);
		}).forEach(titles::add);

		for (Label label : dots)
			LocationUtil.resetLabelRotation(label, 1);

		dots.sort(Comparator.comparing(dot -> Integer.parseInt(dot.getTag().split("\\s++")[0])));
	}

	@Override
	public void execute(User user, App app) {
		final CraftPlayer player = user.getPlayer();

		user.sendMessage((user.isMessages() ? "Отключить" : "Включить") + " экранные сообщения игроков /con");

		if (player.hasPlayedBefore() || user.getExperience() >= EXPERIENCE) {
			val now = System.currentTimeMillis() / 1000;
			B.postpone(10 * 20, () -> {
				if ((now - user.getInfo().getLastTimeRewardClaim()) > REWARD_DELAY_HOURS * 3600) {
					user.getInfo().setLastTimeRewardClaim(now);
					user.setDay(user.getDay() + 1);

					B.postpone(5, () -> Guis.open(player, "daily-reward", player));

					// Бонус к ежедневной награде
					int dailyReward = 10000;
					if (user.getPrefix() != null && user.getPrefix().equals("㧥"))
						dailyReward = dailyReward + 20000;

					AnimationUtil.topTitle(
							user,
							"§aВаша ежедневная награда §6§l%s§f §l+ЛУТБОКС",
							MessageUtil.toMoneyFormat(dailyReward)
					);
					B.postpone(60, () -> {
						val gem = new Gem(ListUtils.random(GemType.values()).name() + ":" + (0.1 + Math.random() / 100 * 70) + ":10000");
						gem.give(user);
						val gemTitle = gem.getType().getTitle();
						new ModTransfer()
								.integer(1)
								.item(CraftItemStack.asNMSCopy(gem.getItem()))
								.string(ChatColor.stripColor(gemTitle + " " + Math.round(gem.getRarity() * 100F) + "%"))
								.string(getRare(gemTitle))
								.send("lootbox", user);
					});
					user.setMoney(user.getMoney() + dailyReward);
				} else {
					AnimationUtil.topTitle(user, "Добро пожаловать в ваш §bМузей§f! 㗩");
				}
			});
			return;
		}

		Cycle.run(5 * 20, titles.size(), iteration -> {
			if (!player.isOnline()) {
				exit();
				return;
			}
			if (iteration >= titles.size() - 1) {
				if (user.getExperience() >= EXPERIENCE)
					player.teleport(dots.get(dots.size() - 1).toCenterLocation());
				user.giveExperience(EXPERIENCE);
				((Museum) user.getState()).giveMenu(user);
				exit();
				return;
			}
			player.sendTitle(titles.get(iteration));
			player.teleport(dots.get(iteration).toCenterLocation());
		});
	}

	private String getRare(String string) {
		return string.contains("⭐⭐⭐") ? "LEGENDARY" : string.contains("⭐⭐") ? "EPIC" : "RARE";
	}
}
