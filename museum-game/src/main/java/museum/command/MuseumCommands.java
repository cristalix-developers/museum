package museum.command;

import clepto.bukkit.B;
import lombok.val;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.Allocation;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.player.User;
import museum.player.prepare.PreparePlayerBrain;
import museum.prototype.Managers;
import museum.util.MessageUtil;
import museum.util.VirtualSign;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

public class MuseumCommands {

	private final App app;
	private static final String PLAYER_OFFLINE_MESSAGE = MessageUtil.get("playeroffline");

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
		B.regCommand(this::cmdBuy, "buy");
		B.regCommand(this::cmdPrefix, "prefix");
		B.regCommand(this::cmdRate, "rate");
	}

	private String cmdPrefix(Player player, String[] args) {
		if (player != null && !player.isOp()) return "§cНеизвестная команда.";
		if (args.length < 2) return "§cИспользование: §e/prefix [Игрок] [Префикс]";
		try {
			User user = app.getUser(Bukkit.getPlayer(args[0]).getUniqueId());
			user.setPrefix(args[1].replace('&', '§').replace('#', '¨'));
			return "§aПрефикс изменён.";
		} catch (NullPointerException ex) {
			return "§cИгрок не найден.";
		}
	}

	private String cmdRunTop(Player player, String[] args) {
		User user = app.getUser(player);
		if (!player.isOp() && user.getLastTopUpdateTime() != 0) return null;
		user.setLastTopUpdateTime(-1);
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
		// Если в инвентаре нет места
		long count = 0L;
		for (Subject subject : user.getSubjects())
			if (!subject.isAllocated() && subject.getPrototype().getType() != SubjectType.MARKER)
				count++;
		if (count > 32)
			return MessageUtil.get("no-free-space");

		if (user.getMoney() < prototype.getPrice()) {
			AnimationUtil.buyFailure(user);
			return null;
		}

		user.setMoney(user.getMoney() - prototype.getPrice());
		// new Subject() писать нельзя - так как нужный класс (CollectorSubject...) не уточнет, и все ломается
		user.getSubjects().add(prototype.provide(new SubjectInfo(
				UUID.randomUUID(),
				prototype.getAddress()
		), user));

		return MessageUtil.get("finally-buy");
	}

	private String cmdVisit(Player sender, String[] args) {
		val user = app.getUser(sender);

		if (!sender.isOp())
			return null;

		if (args.length <= 1)
			return "§cИспользование: §f/museum visit [Игрок] [Музей]";

		val ownerPlayer = Bukkit.getPlayer(args[1]);

		if (ownerPlayer == null || !ownerPlayer.isOnline())
			return PLAYER_OFFLINE_MESSAGE;

		val ownerUser = app.getUser(ownerPlayer);
		String address = args.length > 2 ? args[2] : "main";

		MuseumPrototype prototype = Managers.museum.getPrototype(address);
		Museum museum = prototype == null ? null : ownerUser.getMuseums().get(prototype);
		if (museum == null)
			return MessageUtil.get("museum-not-found");

		if (Objects.equals(user.getLastMuseum(), museum))
			return MessageUtil.get("already-at-home");

		user.setState(museum);

		MessageUtil.find("museum-teleported")
				.set("visitor", user.getName())
				.send(ownerUser);
		return null;
	}

	private String cmdTravel(Player sender, String[] args) {
		val visitor = app.getUser(sender);
		val owner = app.getUser(Bukkit.getPlayer(args[0]));

		if (args.length != 1)
			return null;
		if (owner == null || !owner.getPlayer().isOnline() || owner.getState() == null || owner.equals(visitor)) {
			return PLAYER_OFFLINE_MESSAGE;
		}

		val state = owner.getState();

		if (state instanceof Museum) {
			val museum = (Museum) state;
			if (visitor.getMoney() <= museum.getIncome() / 2) {
				AnimationUtil.buyFailure(visitor);
				return null;
			}

			visitor.setMoney(visitor.getMoney() - museum.getIncome() / 2);
			owner.setMoney(owner.getMoney() + museum.getIncome() / 2);
			visitor.setState(state);
			MessageUtil.find("traveler")
					.set("visitor", visitor.getName())
					.set("price", MessageUtil.toMoneyFormat(museum.getIncome() / 2))
					.send(owner);
		}
		return null;
	}

	private String cmdHome(Player sender, String[] args) {
		val user = this.app.getUser(sender);
		// Не возвращать в музей если игрок возвращается с раскопок
		if (user.getState() == null || (user.getState() instanceof Excavation && ((Excavation) user.getState()).getHitsLeft() < 0))
			return null;
		if (user.getState() instanceof Museum && ((Museum) user.getState()).getOwner().equals(user))
			return MessageUtil.get("already-at-home");

		user.setState(user.getLastMuseum());
		return MessageUtil.get("welcome-home");
	}

	private String cmdSkeleton(Player sender, String[] args) {
		if (!sender.isOp())
			return null;
		Collection<Skeleton> skeletons = this.app.getUser(sender).getSkeletons();
		skeletons.forEach(skeleton ->
				sender.sendMessage("§e" + skeleton.getPrototype().getAddress() + "§f: " + skeleton.getUnlockedFragments().size()));
		return "§e" + skeletons.size() + " in total.";
	}

	private String cmdSubjects(Player sender, String[] args) {
		if (!sender.isOp())
			return null;
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
		if (args.length == 0)
			return "§cИспользование: §e/gui [адрес]";
		try {
			if (sender.getPlayer() == null)
				return null;
			clepto.bukkit.menu.Guis.open(sender, args[0], args.length > 1 ? args[1] : null);
		} catch (NoSuchElementException ex) {
			return MessageUtil.find("no-gui").set("gui", args[0]).getText();
		}
		return null;
	}

	private String cmdShop(Player sender, String[] args) {
		User user = app.getUser(sender);

		if (user.getPlayer() == null)
			return null;

		if (user.getExperience() < PreparePlayerBrain.EXPERIENCE)
			return null;

		if (user.getState() instanceof Excavation)
			return MessageUtil.get("museum-first");
		if (args.length < 1) {
			user.setState(app.getShop());
			return null;
		}
		user.setState(args[0].equals("poly") ? app.getMarket() : app.getShop());
		return null;
	}

	private String cmdChangeTitle(Player sender, String[] args) {
		User user = app.getUser(sender);
		if (!(user.getState() instanceof Museum))
			return MessageUtil.get("not-in-museum");
		if (!((Museum) user.getState()).getOwner().equals(user))
			return MessageUtil.get("root-refuse");
		new VirtualSign().openSign(sender, lines -> {
			if (user.getState() instanceof Museum) {
				val stringBuilder = new StringBuilder();
				for (String line : lines) {
					if (line != null && !line.isEmpty())
						stringBuilder.append(line);
					else
						break;
				}
				((Museum) user.getState()).setTitle(stringBuilder.toString());
				MessageUtil.find("museumtitlechange")
						.set("title", stringBuilder.toString())
						.send(user);
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
					user.sendMessage(PLAYER_OFFLINE_MESSAGE);
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
		if (args.length < 1)
			return "/excavation <место>";
		ExcavationPrototype prototype;
		try {
			prototype = Managers.excavation.getPrototype(args[0]);
		} catch (Exception ignore) {
			return null;
		}
		val level = user.getLevel();
		if (prototype == null || level < PreparePlayerBrain.EXPERIENCE || level < prototype.getRequiredLevel())
			return null;

		if (user.getGrabbedArmorstand() != null)
			return MessageUtil.get("stall-first");

		if (prototype.getPrice() > user.getMoney()) {
			AnimationUtil.buyFailure(user);
			return null;
		}

		user.setMoney(user.getMoney() - prototype.getPrice());

		player.closeInventory();
		user.setState(new Excavation(prototype, prototype.getHitCount()));
		return null;
	}

	private String cmdPickaxe(Player player, String[] args) {
		User user = this.app.getUser(player);
		PickaxeType pickaxe = user.getPickaxeType().getNext();
		if (pickaxe == user.getPickaxeType() || pickaxe == null)
			return null;
		player.closeInventory();

		if (user.getMoney() < pickaxe.getPrice()) {
			AnimationUtil.buyFailure(user);
			return null;
		}

		user.setMoney(user.getMoney() - pickaxe.getPrice());
		user.setPickaxeType(pickaxe);
		player.performCommand("gui pickaxe");
		return MessageUtil.get("newpickaxe");
	}

	private String cmdRate(Player player, String[] args) {
		if (args.length == 0)
			return "/rate <цена>";
		else if (!app.getPlayerDataManager().isRateBegun())
			return "Торги ещё не начались.";

		App.getApp().getPlayerDataManager().getMembers().put(player.getUniqueId(), Integer.parseInt(args[0]));

		return "Ваша ставка была принята.";
	}
}
