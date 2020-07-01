package ru.cristalix.museum.gui;

import clepto.LoveHumans;
import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.Guis;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.PickaxeType;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.excavation.ExcavationPrototype;
import ru.cristalix.museum.gallery.Warp;
import ru.cristalix.museum.museum.Museum;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.util.LevelSystem;
import ru.cristalix.museum.util.MessageUtil;
import ru.cristalix.museum.util.VirtualSign;
import ru.cristalix.museum.util.WarpUtil;

public class MuseumGuis {

	public MuseumGuis(App app) {
		Warp warp = new WarpUtil.WarpBuilder("gallery")
				.onForward(user -> user.getCurrentMuseum().hide(user))
				.build();

		B.regCommand((sender, args) -> {
			Guis.registry.get(args[0]).open(sender, args.length > 1 ? args[1] : null);
			return null;
		}, "gui");

		B.regCommand((sender, args) -> {
			warp.warp(app.getUser(sender));
			return null;
		}, "gallery");

		B.regCommand((sender, args) -> {
			new VirtualSign().openSign(sender, lines -> {
				for (String line : lines) {
					if (line != null && !line.isEmpty()) {
						User user = app.getUser(sender);
						user.getCurrentMuseum().setTitle(line);
						MessageUtil.find("museumtitlechange")
								.set("title", line)
								.send(user);
					}
				}
			});
			return null;
		}, "changetitle");

		B.regCommand((sender, args) -> {
			new VirtualSign().openSign(sender, lines -> {
				for (String line : lines) {
					if (line != null && !line.isEmpty()) {
						Player invited = Bukkit.getPlayer(line);
						User user = app.getUser(sender);
						if (invited == null) {
							MessageUtil.find("playeroffline").send(user);
							return;
						} else if (invited.equals(sender)) {
							MessageUtil.find("inviteyourself").send(user);
							return;
						}
						MessageUtil.find("invited").send(user);
						TextComponent invite = new TextComponent(
								MessageUtil.find("invitefrom")
										.set("player", sender.getName())
										.getText()
						);
						invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/museum accept " + sender.getName()));
						invited.sendMessage(invite);
					}
				}
			});
			return null;
		}, "invite");

		B.regCommand((player, args) -> {
			User user = app.getUser(player);
			if (args.length == 0)
				return null;
			ExcavationPrototype proto = Managers.excavation.getPrototype(args[0]);
			if (proto == null)
				return null;

			player.closeInventory();

			if (proto.getPrice() > user.getMoney())
				return MessageUtil.get("nomoney");

			user.setMoney(user.getMoney() - proto.getPrice());

			Excavation excavation = new Excavation(proto, proto.getHitCount());
			user.setExcavation(excavation);

			user.getCurrentMuseum().hide(user);
			excavation.load(user);
			return null;
		}, "excavation", "exc");

		B.regCommand((player, args) -> {
			User user = app.getUser(player);
			PickaxeType pickaxe = user.getPickaxeType().getNext();
			if (pickaxe == user.getPickaxeType())
				return null;
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
			ExcavationPrototype excavation = Managers.excavation.getPrototype(
					context.getOpenedGui().getSlotData(slotId).getInfo()
			);
			if (excavation == null || excavation.getRequiredLevel() > app.getUser(player).getLevel())
				return Lemonade.get("unavailable").render();
			return base.dynamic()
					.fill("excavation", excavation.getTitle())
					.fill("cost", String.format("%.2f", excavation.getPrice()))
					.fill("lvl", String.valueOf(excavation.getRequiredLevel()))
					.fill("breaks", String.valueOf(excavation.getHitCount()))
					.render();
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

		Guis.registerItemizer("museum", (base, player, context, slotId) -> {
			User user = app.getUser(player);
			Museum museum = user.getCurrentMuseum();
			return base.dynamic()
					.fill("owner", museum.getOwner().getName())
					.fill("title", museum.getTitle())
					.fill("views", String.valueOf(museum.getViews()))
					.fill("income", MessageUtil.toMoneyFormat(museum.getIncome()))
					.fill("spaces", String.valueOf(museum.getSubjects().size()))
					.fill("sinceCreation", LoveHumans.formatTime(System.currentTimeMillis() - museum.getCreationDate().getTime()))
					.render();
		});
	}

}
