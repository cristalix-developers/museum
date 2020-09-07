package museum.player.prepare;

import clepto.cristalix.mapservice.Label;
import com.destroystokyo.paper.Title;
import museum.App;
import museum.player.User;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author func 26.08.2020
 * @project museum
 */
public class PreparePlayerBrain implements Prepare {

	private final List<Label> dots;
	private final List<Title> titles = Arrays.asList(
			new Title("Привет!"),
			new Title("Это.", "Твой. Музей."),
			new Title("Заполняй витрины"),
			new Title("Находи динозавров"),
			new Title("Собирай монеты"),
			new Title("Кастомизируй"),
			new Title("Играй с друзьями"),
			new Title("Удачи!")
													);

	public PreparePlayerBrain(App app) {
		dots = app.getMap().getLabels("guide");

		for (Label label : dots) {
			String[] ss = label.getTag().split("\\s++");

			label.setYaw(Integer.parseInt(ss[1]));
			label.setPitch(Integer.parseInt(ss[2]));
		}

		dots.sort(Comparator.comparing(dot -> Integer.parseInt(dot.getTag().split("\\s++")[0])));
	}

	@Override
	public void execute(User user, App app) {
		if (user.getPlayer().hasPlayedBefore() || user.getExperience() > 100)
			return;

		new BukkitRunnable() {
			int counter = 0;

			@Override
			public void run() {
				if (!user.getPlayer().isOnline()) {
					this.cancel();
					return;
				}

				if (counter >= titles.size()) {

					user.getPlayer().sendMessage("§f[§aВНИМАНИЕ§f]");
					user.getPlayer().sendMessage("§6У входа §fв музей вас ждет §6Рафаэль,");
					user.getPlayer().sendMessage("§fон может устроить §6раскопки!");
					user.getPlayer().sendMessage("§6Внутри музея, Сатоши §fможет показать вам");
					user.getPlayer().sendMessage("§6постройки §fдля кастомизации помещения.");

					this.cancel();
					return;
				}
				user.getPlayer().sendTitle(titles.get(counter));
				user.getPlayer().teleport(dots.get(counter).toCenterLocation());
				counter++;
			}
		}.runTaskTimer(app, 3 * 20L, 7 * 20L);
	}

}
