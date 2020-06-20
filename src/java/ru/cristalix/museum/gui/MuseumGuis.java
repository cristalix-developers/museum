package ru.cristalix.museum.gui;

import clepto.LoveHumans;
import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.Guis;
import clepto.bukkit.gui.SlotData;
import org.bukkit.Statistic;
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.util.MessageUtil;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.excavation.ExcavationPrototype;

public class MuseumGuis {

	public static void init(App app) {
		B.regCommand((sender, args) -> {
			Guis.registry.get(args[0]).open(sender, args[1]);
			return null;
		}, "gui");

		B.regCommand((player, args) -> {
			player.closeInventory();
			User user = app.getUser(player);
			ExcavationPrototype proto = app.getExcavationManager().getExcavationPrototype(args[0]);

			if (proto.getPrice() > user.getMoney())
				return MessageUtil.get("nomoney");

			user.setMoney(user.getMoney() - proto.getPrice());

			Excavation excavation = new Excavation(proto);
			user.setExcavation(excavation);

			user.getCurrentMuseum().unload(user);
			excavation.load(user);
			return "";
		}, "excavation", "exc");

		B.regCommand((player, args) -> {
			User user = app.getUser(player);
			PickaxeType pickaxe = user.getPickaxeType().getNext();
			if (pickaxe == user.getPickaxeType()) return "";
			player.closeInventory();

			if (user.getMoney() < pickaxe.getPrice())
				return MessageUtil.get("nomoney");

			user.setPickaxeType(pickaxe);
			user.setMoney(user.getMoney() - pickaxe.getPrice());
			player.performCommand("gui pickaxes");
			return MessageUtil.get("newpickaxe");

		}, "pickaxe");

		Guis.registerItemizer("upgrade-pickaxe", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			PickaxeType pickaxe = user.getPickaxeType().getNext();
			return Lemonade.get("pickaxe-" + pickaxe.name()).render();
		});
		
		Guis.registerItemizer("excavation", (base, player, context, slotId) -> {
			SlotData slotData = context.getOpenedGui().getSlotData(slotId);
			String info = slotData.getInfo();
			ExcavationPrototype excavation = app.getExcavationManager().getExcavationPrototype(info);
			User user = App.getApp().getUser(player);
			if (excavation == null || excavation.getRequiredLevel() > user.getLevel()) return Lemonade.get("unavailable").render();
			return base.dynamic()
					.fill("excavation", excavation.getTitle())
					.fill("cost", String.format("%.2f", excavation.getPrice()))
					.fill("lvl", String.valueOf(excavation.getRequiredLevel()))
					.fill("breaks", String.valueOf(excavation.getHitCount()))
					.render();
		});

		Guis.registerItemizer("profile", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			return base.dynamic().fill("level", String.valueOf(user.getLevel()))
					.fill("money", MessageUtil.toMoneyFormat(user.getMoney()))
					.fill("exp", String.valueOf(user.getExperience()))
					.fill("need_exp", String.valueOf(user.getRequiredExperience(user.getLevel() + 1)))
					.fill("hours_played", String.valueOf(player.getStatistic(Statistic.PLAY_ONE_TICK) / 720_000))
					.fill("coins_picked", String.valueOf(user.getPickedCoinsCount()))
					.fill("pickaxe", user.getPickaxeType().name())
					.fill("excavations", String.valueOf(user.getExcavationCount()))
					.fill("fragments", String.valueOf(user.getSkeletons().values().stream().mapToInt(s -> s.getUnlockedFragments().size()).sum()))
					.render();
		});

		Guis.registerItemizer("museum", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			Museum museum = user.getCurrentMuseum();
			return base.dynamic()
					.fill("owner", museum.getOwner().getName())
					.fill("title", museum.getTitle())
					.fill("views", String.valueOf(museum.getViews()))
					.fill("income", MessageUtil.toMoneyFormat(museum.getIncome()))
					.fill("collectors", String.valueOf(museum.getCollectorSlots()))
					.fill("spaces", String.valueOf(museum.getSubjects().size()))
					.fill("sinceCreation", LoveHumans.formatTime(System.currentTimeMillis() - museum.getCreationDate().getTime()))
					.render();
		});
	}

}