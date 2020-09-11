package museum.command;

import clepto.bukkit.B;
import clepto.bukkit.gui.Gui;
import clepto.bukkit.gui.Guis;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.museum.map.SkeletonSubjectPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.Allocation;
import museum.museum.subject.CollectorSubject;
import museum.museum.subject.SkeletonSubject;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import museum.util.VirtualSign;
import museum.util.warp.Warp;
import museum.util.warp.WarpUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class MuseumCommands {

	private final App app;
	private final Warp galleryWarp;

	public MuseumCommands(App app) {
		this.app = app;

		this.galleryWarp = new WarpUtil.WarpBuilder("gallery")
				.onForward(user -> user.getCurrentMuseum().hide(user))
				.build();

		B.regCommand(this::cmdHome, "home", "leave", "spawn");
		B.regCommand(this::cmdSubjects, "subjects", "sj");
		B.regCommand(this::cmdGui, "gui");
		B.regCommand(this::cmdGallery, "gallery");
		B.regCommand(this::cmdChangeTitle, "changetitle");
		B.regCommand(this::cmdInvite, "invite");
		B.regCommand(this::cmdExcavation, "excavation", "exc");
		B.regCommand(this::cmdPickaxe, "pickaxe");
		B.regCommand(this::cmdSubject, "subject");

	}

	private String cmdHome(Player sender, String[] args) {
		User user = this.app.getUser(sender);
		if (user.getExcavation() != null)
			user.setExcavation(null);
		else if (user.getCurrentMuseum().getOwner() != user)
			user.getCurrentMuseum().hide(user);
		else
			return MessageUtil.find("already-at-home").getText();
		user.getMuseums().supply(Managers.museum.getPrototype("main")).show(user);
		return MessageUtil.find("welcome-home").getText();
	}

	private String cmdSubjects(Player sender, String[] args) {
		List<Subject> subjects = this.app.getUser(sender).getCurrentMuseum().getSubjects();
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
	}

	private String cmdGui(Player sender, String[] args) {
		this.app.getUser(sender);
		if (args.length == 0) return "§cИспользование: §e/gui [адрес]";
		Gui gui = Guis.registry.get(args[0]);
		if (gui == null) return "§cГуи с адресом §e" + args[0] + "§c не найден.";

		gui.open(sender, args.length > 1 ? args[1] : null);
		return null;
	}

	private String cmdGallery(Player sender, String[] args) {
		this.galleryWarp.warp(this.app.getUser(sender));
		return null;
	}

	private String cmdChangeTitle(Player sender, String[] args) {
		new VirtualSign().openSign(sender, lines -> {
			for (String line : lines) {
				if (line != null && !line.isEmpty()) {
					User user = this.app.getUser(sender);
					user.getCurrentMuseum().setTitle(line);
					MessageUtil.find("museumtitlechange")
							.set("title", line)
							.send(user);
				}
			}
		});
		return null;
	}

	private String cmdInvite(Player sender, String[] args) {
		new VirtualSign().openSign(sender, lines -> {
			for (String line : lines) {
				if (line != null && !line.isEmpty()) {
					Player invited = Bukkit.getPlayer(line);
					User user = this.app.getUser(sender);
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
	}

	private String cmdExcavation(Player player, String[] args) {
		User user = this.app.getUser(player);
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
	}

	private String cmdPickaxe(Player player, String[] args) {
		User user = this.app.getUser(player);
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

	}

	private String cmdSubject(Player player, String[] args) {
		if (args.length < 2)
			return null;

		UUID subjectUuid;

		try {
			subjectUuid = UUID.fromString(args[1]);
		} catch (IllegalArgumentException ignored) {
			return null;
		}

		val user = app.getUser(player);
		Museum museum = user.getCurrentMuseum();
		val subject = museum.getSubjectByUuid(subjectUuid);
		if (museum.getOwner() != user)
			return "Вы не в своей тарелке, откуда вы знаете uuid субъекта?))))";

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
		} else if ("setdino".equals(args[0]) && args.length == 3) {
			if (!(subject instanceof SkeletonSubject))
				return null;
			val skeletonSubject = (SkeletonSubject) subject;
			val previousSkeleton = skeletonSubject.getSkeleton();

			SkeletonPrototype newSkeletonType = null;
			try {
				newSkeletonType = Managers.skeleton.getByIndex(Integer.parseInt(args[2]));
			} catch (Exception ignored) {}

			if (newSkeletonType == null) skeletonSubject.setSkeleton(null);
			else {
				// Если этот скелет уже выставлен на другой витрине, потребовать сперва убрать его оттуда
				for (val anotherSubject : museum.getSubjects(SubjectType.SKELETON_CASE)) {
					if (anotherSubject == subject)
						continue;
					val skeleton = anotherSubject.getSkeleton();
					if (skeleton == null) continue;
					if (skeleton.getCachedInfo().getPrototypeAddress().equals(newSkeletonType.getAddress())) {
						user.closeInventory();
						return MessageUtil.find("standlocked").getText();
					}
				}

				if (newSkeletonType.getSize() > ((SkeletonSubjectPrototype) subject.getPrototype()).getSize())
					return "не влазит толстый))0"; // ToDo: в конфиг


				val newSkeleton = user.getSkeletons().get(newSkeletonType);

				if (newSkeleton == null || newSkeleton == previousSkeleton)
					return null;

				skeletonSubject.setSkeleton(newSkeleton);
			}

			for (User watcher : app.getUsers()) {
				if (watcher.getCurrentMuseum() != museum) continue;
				if (previousSkeleton != null)
					previousSkeleton.getPrototype().hide(watcher.getPlayer());
				skeletonSubject.show(watcher);
			}

			player.closeInventory();
			return MessageUtil.find(newSkeletonType == null ? "freestand" : "standplaced").getText();
		}
		return null;
	}

}
