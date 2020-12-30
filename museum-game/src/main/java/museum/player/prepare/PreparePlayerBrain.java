package museum.player.prepare;

import clepto.bukkit.B;
import clepto.bukkit.Cycle;
import clepto.bukkit.world.Label;
import com.destroystokyo.paper.Title;
import lombok.val;
import museum.App;
import museum.client_conversation.ClientPacket;
import museum.museum.Museum;
import museum.player.User;
import museum.util.LocationUtil;
import museum.util.MessageUtil;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;

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

		if (player.hasPlayedBefore() || user.getExperience() >= EXPERIENCE) {
			val now = System.currentTimeMillis() / 1000;
			B.postpone(15 * 20, () -> {
				if ((now - user.getInfo().getLastTimeRewardClaim()) > REWARD_DELAY_HOURS * 3600) {
					user.getInfo().setLastTimeRewardClaim(now);

					// Бонус к ежедневной награде
					int dailyReward = 10000;
					if (user.getPrefix() != null && user.getPrefix().equals("㧥"))
						dailyReward = dailyReward + 20000;

					ClientPacket.sendTopTitle(user, "§aВаша ежедневная награда §6§l" + MessageUtil.toMoneyFormat(dailyReward) + "§f, §b§l15 опыта§f, §d20㦶");
					user.setMoney(user.getMoney() + dailyReward);
					user.giveExperience(15);
					user.setCrystal(user.getCrystal() + 20);
				} else {
					ClientPacket.sendTopTitle(user, "С наступающим §bНовым Годом§f, приятной игры! 㗩");
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
}
