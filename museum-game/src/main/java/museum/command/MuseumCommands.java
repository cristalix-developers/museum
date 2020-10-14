package museum.command;

import clepto.bukkit.B;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.Allocation;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.player.User;
import museum.player.prepare.PreparePlayerBrain;
import museum.prototype.Managers;
import museum.util.MessageUtil;
import museum.util.VirtualSign;
import museum.visitor.VisitorHandler;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

public class MuseumCommands {

	private final App app;

	public MuseumCommands(App app) {
		this.app = app;

		B.regCommand(this::cmdHome, "home", "leave", "spawn");
		B.regCommand(this::cmdSubjects, "subjects", "sj");
		B.regCommand(this::cmdGui, "gui");
		B.regCommand(this::cmdShop, "shop", "gallery");
		B.regCommand(this::cmdChangeTitle, "changetitle");
		B.regCommand(this::cmdInvite, "invite");
		B.regCommand(this::cmdExcavation, "excavation", "exc");
		B.regCommand(this::cmdPickaxe, "pickaxe");
		B.regCommand(this::cmdSkeleton, "skeleton");
		B.regCommand(this::cmdRunTop, "runtop", "rt");
		B.regCommand(this::cmdTravel, "travel");
		B.regCommand(this::cmdVisit, "visit", "museum");
		B.regCommand((sender, args) -> VisitorHandler.getVisitorUuids().values().stream()
				.distinct().findFirst().get().getCurrentRoute()
				.stream().map(String::valueOf)
				.collect(Collectors.joining("\n")), "routes");
		B.regCommand(this::cmdBuy, "buy");
	}

	private String cmdRunTop(Player player, String[] args) {
		if (player.isOp()) {
			app.getTopManager().updateData();
			app.getTopManager().sendTops();
		}
		return null;
	}

	private String cmdBuy(Player sender, String[] args) {
		if (args.length == 0)
			return "§cИспользование: §f/buy [subject-address]";

		SubjectPrototype prototype;
		try {
			prototype = Managers.subject.getPrototype(args[0]);
		} catch (Exception e) {
			return e.getMessage();
		}
		if (prototype == null)
			return null;

		val user = app.getUser(sender);

		if (user.getMoney() < prototype.getPrice())
			return MessageUtil.get("nomoney");

		user.setMoney(user.getMoney() - prototype.getPrice());
		user.getSubjects().add(new Subject(prototype, new SubjectInfo(UUID.randomUUID(), prototype.getAddress()), user));

		return MessageUtil.get("finally-buy");
	}

	private String cmdVisit(Player sender, String[] args) {
		val user = app.getUser(sender);

		if (args.length == 0)
			return "§cИспользование: §f/museum visit [Игрок] [Музей]";

		val ownerPlayer = Bukkit.getPlayer(args[1]);

		if (ownerPlayer == null || !ownerPlayer.isOnline())
			return MessageUtil.get("playeroffline");

		val ownerUser = app.getUser(ownerPlayer);
		String address = args.length > 2 ? args[2] : "main";

		MuseumPrototype prototype = Managers.museum.getPrototype(address);
		Museum museum = prototype == null ? null : ownerUser.getMuseums().get(prototype);
		if (museum == null)
			return MessageUtil.find("museum-not-found").set("type", address).getText();

		if (user.getLastMuseum().equals(museum))
			return MessageUtil.get("already-at-home");

		user.setState(museum);

		return MessageUtil.find("museum-teleported")
				.set("visitor", user.getName())
				.getText();
	}

	private String cmdTravel(Player sender, String[] args) {
		val visitor = app.getUser(sender);
		val owner = app.getUser(Bukkit.getPlayer(args[0]));

		if (args.length < 2)
			return null;
		if (owner == null || !owner.getPlayer().isOnline() || owner.getState() == null || owner.equals(visitor)) {
			return MessageUtil.get("playeroffline");
		}

		double price;

		try {
			price = Double.parseDouble(args[1]);
		} catch (Exception ignored) {
			return null;
		}

		val state = owner.getState();

		if (state instanceof Museum) {
			if (visitor.getMoney() <= price)
				return MessageUtil.get("nomoney");

			visitor.setMoney(visitor.getMoney() - price);
			owner.setMoney(owner.getMoney() + price);
			visitor.setState(state);
			MessageUtil.find("traveler")
					.set("visitor", visitor.getName())
					.set("price", MessageUtil.toMoneyFormat(price))
					.send(owner);
		}
		return null;
	}

	private String cmdHome(Player sender, String[] args) {
		val user = this.app.getUser(sender);
		if (user.getState() instanceof Museum && ((Museum) user.getState()).getOwner().equals(user))
			return MessageUtil.get("already-at-home");
		user.setState(user.getLastMuseum() == null ?
				user.getMuseums().get(Managers.museum.getPrototype("main")) :
				user.getLastMuseum()
		);
		return MessageUtil.get("welcome-home");
	}

	private String cmdSkeleton(Player sender, String[] args) {
		Collection<Skeleton> skeletons = this.app.getUser(sender).getSkeletons();
		skeletons.forEach(skeleton ->
				sender.sendMessage("§e" + skeleton.getPrototype().getAddress() + "§f: " + skeleton.getUnlockedFragments().size()));
		return "§e" + skeletons.size() + " in total.";
	}

	private String cmdSubjects(Player sender, String[] args) {
		Collection<Subject> subjects = this.app.getUser(sender).getSubjects();
		for (Subject subject : subjects) {
			String allocationInfo = "§cno allocation";
			Allocation allocation = subject.getAllocation();
			if (allocation != null) {
				Location origin = allocation.getOrigin();
				allocationInfo = allocation.getUpdatePackets().size() + " packets, §f" + origin.getX() + " " + origin.getY() + " " + origin.getZ();
			}
			sender.sendMessage("§e" + subject.getPrototype().getAddress() + "§f: " + subject.getOwner().getName() + ", " + allocationInfo);
		}
		return "§e" + subjects.size() + " in total.";
	}

	private String cmdGui(Player sender, String[] args) {
		this.app.getUser(sender);
		if (args.length == 0) return "§cИспользование: §e/gui [адрес]";
		try {
			clepto.bukkit.menu.Guis.open(sender, args[0], args.length > 1 ? args[1] : null);
		} catch (NoSuchElementException ex) {
			return "§cГуи с адресом §e" + args[0] + "§c не найден.";
		}
		return null;
	}

	private String cmdShop(Player sender, String[] args) {
		User user = app.getUser(sender);

		if (user.getExperience() < PreparePlayerBrain.EXPERIENCE)
			return null;

		if (user.getState() instanceof Excavation)
			return "§cВы на раскопках, сперва вернитесь домой";
		user.setState(app.getShop());
		return null;
	}

	private String cmdChangeTitle(Player sender, String[] args) {
		User user = app.getUser(sender);
		if (!(user.getState() instanceof Museum))
			return MessageUtil.get("not-in-museum");
		if (!((Museum) user.getState()).getOwner().equals(user))
			return MessageUtil.get("root-refuse");
		new VirtualSign().openSign(sender, lines -> {
			for (String line : lines) {
				if (line != null && !line.isEmpty()) {
					((Museum) user.getState()).setTitle(line);
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
				if (line == null || line.isEmpty())
					return;
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
		});
		return null;
	}

	private String cmdExcavation(Player player, String[] args) {
		User user = this.app.getUser(player);
		if (args.length == 0)
			return "/excavation <место>";
		ExcavationPrototype proto = Managers.excavation.getPrototype(args[0]);
		if (proto == null)
			return "Такого места для раскопок нет";

		if (user.getExperience() < PreparePlayerBrain.EXPERIENCE)
			return "Опыта мало";

		player.closeInventory();

		if (proto.getPrice() > user.getMoney())
			return MessageUtil.get("nomoney");

		user.setMoney(user.getMoney() - proto.getPrice());

		user.setState(new Excavation(proto, proto.getHitCount()));

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
		player.performCommand("gui pickaxe");
		return MessageUtil.get("newpickaxe");
	}
}
