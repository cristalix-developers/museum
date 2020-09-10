package museum.gui;

import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.Guis;
import clepto.humanize.TimeFormatter;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Color;

import java.time.Duration;

public class MuseumGuis {

	public MuseumGuis(App app) {

		Guis.registerItemizer("upgrade-pickaxe", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			PickaxeType pickaxe = user.getPickaxeType().getNext();
			return Lemonade.get("pickaxe-" + pickaxe.name()).render();
		});

		Guis.registerItemizer("subject-color", (base, player, context, slotId) -> {
			String info = context.getOpenedGui().getSlotData(slotId).getInfo();
			Color color = Color.valueOf(info.toUpperCase());
			ItemStack item = base.dynamic().fill("color-name", color.getTeamName()).render();
			item.setDurability((short) color.getWoolData());
			return item;
		});

		Guis.registerItemizer("excavation", (base, player, context, slotId) -> {
			ExcavationPrototype excavation = Managers.excavation.getPrototype(
					context.getOpenedGui().getSlotData(slotId).getInfo()
			);
			if (excavation == null  || excavation.getRequiredLevel() > app.getUser(player).getLevel())
				return Lemonade.get("unavailable").render();
			val item = base.dynamic()
					.fill("excavation", excavation.getTitle())
					.fill("cost", String.format("%.2f", excavation.getPrice()))
					.fill("lvl", String.valueOf(excavation.getRequiredLevel()))
					.fill("breaks", String.valueOf(excavation.getHitCount()))
					.render();
			item.setType(excavation.getIcon());
			return item;
		});

		Guis.registerItemizer("profile", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			return base.dynamic()
					.fill("level", String.valueOf(user.getLevel()))
					.fill("money", MessageUtil.toMoneyFormat(user.getMoney()))
					.fill("exp", String.valueOf(user.getExperience()))
					.fill("need_exp", LevelSystem.formatExperience(user.getExperience()))
					.fill("hours_played", String.valueOf(player.getStatistic(Statistic.PLAY_ONE_TICK) / 720_000))
					.fill("coins_picked", String.valueOf(user.getPickedCoinsCount()))
					.fill("pickaxe", user.getPickaxeType().name())
					.fill("excavations", String.valueOf(user.getExcavationCount()))
					.fill("fragments", String.valueOf(user.getSkeletons().stream().mapToInt(s -> s.getUnlockedFragments().size()).sum()))
					.render();
		});

		TimeFormatter formatter = TimeFormatter.builder().accuracy(500).build();

		Guis.registerItemizer("museum", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			Museum museum = user.getCurrentMuseum();
			return base.dynamic()
					.fill("owner", museum.getOwner().getName())
					.fill("title", museum.getTitle())
					.fill("views", String.valueOf(museum.getViews()))
					.fill("income", MessageUtil.toMoneyFormat(museum.getIncome()))
					.fill("spaces", String.valueOf(museum.getSubjects().size()))
					.fill("sinceCreation", formatter.format(Duration.ofMillis(System.currentTimeMillis() - museum.getCreationDate().getTime())))
					.render();
		});
	}

}
