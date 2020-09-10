package museum.gui;

import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.Gui;
import clepto.bukkit.gui.Guis;
import clepto.humanize.TimeFormatter;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.museum.map.SubjectType;
import museum.museum.subject.Allocation;
import museum.museum.subject.CollectorSubject;
import museum.museum.subject.SkeletonSubject;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import museum.util.VirtualSign;
import museum.util.warp.Warp;
import museum.util.warp.WarpUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Color;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuseumGuis {

	public static final ItemStack AIR_ITEM = new ItemStack(Material.AIR);
	private static final ItemStack LOCK = Lemonade.get("lock").render();

	public MuseumGuis(App app) {
		Warp warp = new WarpUtil.WarpBuilder("gallery")
				.onForward(user -> user.getCurrentMuseum().hide(user))
				.build();

		B.regCommand((sender, args) -> {
			User user = app.getUser(sender);
			if (user.getExcavation() != null)
				user.setExcavation(null);
			else if (user.getCurrentMuseum().getOwner() != user)
				user.getCurrentMuseum().hide(user);
			else
				return MessageUtil.find("already-at-home").getText();
			user.getMuseums().get(Managers.museum.getPrototype("main")).show(user);
			return MessageUtil.find("welcome-home").getText();
		}, "home", "leave", "spawn");

		B.regCommand((sender, args) -> {
			List<Subject> subjects = app.getUser(sender).getCurrentMuseum().getSubjects();
			for (Subject subject : subjects) {
				String allocationInfo = "§cno allocation";
				Allocation allocation = subject.getAllocation();
				if (allocation != null) {
					Location origin = allocation.getOrigin();
					allocationInfo = allocation.getAllocatedBlocks().size() + " blocks, §f" + origin.getX() + " " + origin.getY() + " " + origin.getZ();
				}
				sender.sendMessage("§e" + subject.getPrototype().getAddress() + "§f: " + subject.getOwner().getName() + ", " + allocationInfo);
			}
			return "§e" + subjects.size() + " in total.";
		}, "subjects", "sj");

		B.regCommand((sender, args) -> {
			if (args.length == 0) return "§cИспользование: §e/gui [адрес]";
			Gui gui = Guis.registry.get(args[0]);
			if (gui == null) return "§cМеню с адресом §e" + args[0] + "§c не найдено.";

			gui.open(sender, args.length > 1 ? args[1] : null);
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

		int dinoOffset = 9;
		val dinosaurs = new ArrayList<>(Managers.skeleton.getMap().values());

		B.regCommand((player, args) -> {
			if (args.length < 2)
				return null;

			UUID subjectUuid;

			try {
				subjectUuid = UUID.fromString(args[1]);
			} catch (IllegalArgumentException ignored) {
				return null;
			}

			val user = app.getUser(player);
			val subject = user.getCurrentMuseum().getSubjectByUuid(subjectUuid);

			if (subject == null)
				return null;

			if ("special".equals(args[0])) {
				if (subject instanceof SkeletonSubject)
					player.performCommand("gui skeleton-manipulator " + subjectUuid.toString());
				else if (subject instanceof CollectorSubject)
					player.performCommand("gui collector-manipulator " + subjectUuid.toString());
			} else if ("destroy".equals(args[0])) {
				if (!subject.isAllocated())
					return null;
				subject.getAllocation().getDestroyPackets().forEach(user::sendPacket);
				subject.hide(user);
				subject.allocate(null);

				player.getInventory().addItem(SubjectLogoUtil.encodeSubjectToItemStack(subject));
				player.closeInventory();
				return MessageUtil.find("destroyed").getText();
			} else if ("cleardino".equals(args[0])) {
				if (!(subject instanceof SkeletonSubject))
					return null;
				((SkeletonSubject) subject).clear(user);
				player.closeInventory();
				return MessageUtil.find("freestand").getText();
			} else if ("setdino".equals(args[0]) && args.length == 3) {
				if (!(subject instanceof SkeletonSubject))
					return null;

				Skeleton currentSkeleton = null;

				String dinosaur;
				try {
					dinosaur = dinosaurs.get(Integer.parseInt(args[2])).getAddress();
				} catch (Exception e) {
					return null;
				}

				for (Skeleton skeleton : user.getSkeletons())
					if (skeleton.getCachedInfo().getPrototypeAddress().equals(dinosaur))
						currentSkeleton = skeleton;

				if (currentSkeleton == null)
					return null;

				// todo: Не забыть про проверку на занятость другой витриной

				((SkeletonSubject) subject).setSkeleton(user, currentSkeleton);
				player.closeInventory();
				return MessageUtil.find("standplaced").getText();
			}
			return null;
		}, "subject");

		Guis.registerItemizer("subjects-select-dino", (base, player, context, slotId) -> {
			if (slotId < dinoOffset)
				return AIR_ITEM;

			SkeletonPrototype prototype;

			try {
				prototype = dinosaurs.get(slotId - dinoOffset);
			} catch (IndexOutOfBoundsException e) {
				return LOCK;
			}

			val user = app.getUser(player);

			// Если любая витрина уже использует этот прототип, то поставить lock предмет
			for (SkeletonSubject skeletonSubject : user.getCurrentMuseum().getSubjects(SubjectType.SKELETON_CASE))
				if (skeletonSubject.getSkeleton().getCachedInfo().getPrototypeAddress().equals(prototype.getAddress()))
					return LOCK;

			/*base.dynamic().fill("")
			user.getSkeletonInfos().
			val item = base.dynamic().
					item.setDurability((short) color.getWoolData());
					User user = app.getUser(player);
			PickaxeType pickaxe = user.getPickaxeType().getNext();
			return Lemonade.get("pickaxe-" + pickaxe.name()).render();*/
			return null;
		});

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
			if (excavation == null || excavation.getRequiredLevel() > app.getUser(player).getLevel())
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
