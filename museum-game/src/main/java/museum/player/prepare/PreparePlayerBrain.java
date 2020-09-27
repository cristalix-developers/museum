package museum.player.prepare;

import clepto.bukkit.Cycle;
import clepto.cristalix.mapservice.Label;
import com.destroystokyo.paper.Title;
import museum.App;
import museum.museum.Museum;
import museum.player.User;
import museum.util.LocationUtil;
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

	public static final PreparePlayerBrain INSTANCE = new PreparePlayerBrain();
	public static final int EXPERIENCE = 10;

	private final List<Label> dots;
	private final List<Title> titles = new ArrayList<>();
	private final String endMessage = "§f[§aВНИМАНИЕ§f]\n" +
			"§6У входа §fв музей вас ждет §6Рафаэль,\n" +
			"§fон может устроить §6раскопки!\n" +
			"§6Внутри музея, Сатоши §fможет показать вам\n" +
			"§6постройки §fдля кастомизации помещения.\n";

	public PreparePlayerBrain() {
		dots = App.getApp().getMap().getLabels("guide");

		// При next след. текст становится на второе место
		Stream.of(
				"Привет! 䀈", "Это.nextТвой. Музей. 㸾", "Заполняйnextвитрины 㜤",
				"Раскапывайnextдинозавров 㿿", "Собирай монеты 㜰", "Кастомизируй 㟡",
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

		if (player.hasPlayedBefore() || user.getExperience() >= EXPERIENCE)
			return;

		Cycle.run(5 * 20, titles.size(), iteration -> {
				if (!player.isOnline()) {
					exit();
					return;
				}
				if (iteration >= titles.size()) {
					player.sendMessage(endMessage);
					user.giveExperience(EXPERIENCE);
					((Museum) user.getState()).giveMenu();
					exit();
					return;
				}
				player.sendTitle(titles.get(iteration));
				player.teleport(dots.get(iteration).toCenterLocation());
		});
	}
}
