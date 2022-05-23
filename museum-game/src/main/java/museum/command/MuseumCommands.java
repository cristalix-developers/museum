package museum.command;

import clepto.bukkit.B;
import lombok.val;
import me.func.mod.Anime;
import me.func.mod.Glow;
import me.func.mod.selection.Button;
import me.func.mod.selection.Selection;
import me.func.protocol.GlowColor;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.fragment.Fragment;
import museum.fragment.Gem;
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
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Formatting;
import ru.cristalix.core.item.Items;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.GetAccountBalancePackage;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		B.regCommand(this::cmdResourcePack, "resourcepack");
		B.regCommand(this::cmdDonate, "donate");
		B.regCommand(this::cmdMenu, "menu");
		B.regCommand(this::cmdLootBox, "lootbox");
		B.regCommand(this::cmdPolishing, "polishing");
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

	private final List<Button> menuButtons = new ArrayList<>(Arrays.asList(
			new Button()
					.texture("minecraft:textures/items/sign.png")
					.title("§bПереименовать музей")
					.description("§7Если вам не нравится\n" +
							"§7название вашего музея\n" +
							"§7вы можете его изменить."
					).onClick((click, index, button) -> click.performCommand("changetitle")),
			new Button()
					.texture("minecraft:textures/items/end_crystal.png")
					.title("§bПрефиксы")
					.description("§7Выберите префикс!\n" +
							"§7Некоторые редкие префиксы\n" +
							"§7дают бонусы."
					).onClick((click, index, button) -> click.performCommand("prefixes")),
			new Button()
					.material(Material.ENDER_PEARL)
					.title("§bДень/Ночь")
					.description("§7Меняйте режим так,", "§7как нравится глазам!"
					).onClick((click, index, button) -> {
						val user = App.getApp().getUser(click);
						user.getPlayer().setPlayerTime(user.getInfo().isDarkTheme() ? 12000 : 21000, true);
						user.getInfo().setDarkTheme(!user.getInfo().isDarkTheme());
						click.closeInventory();
					}),
			new Button()
					.texture("minecraft:textures/items/golden_pickaxe.png")
					.title("§bИнструменты")
					.description("§7Улучшайте ваше снаряжение."
					).onClick((click, index, button) -> click.performCommand("gui tools")),
			new Button()
					.texture("minecraft:textures/items/compass.png")
					.title("§bЭкспедиции")
					.description("§7Отправляйтесь на раскопки\n" +
							"§7и найдите следы прошлого."
					).onClick((click, index, button) -> click.performCommand("gui excavation")),
			new Button()
					.texture("minecraft:textures/items/writable_book.png")
					.title("§bПригласить друга")
					.description("§7Нажмите и введите\n" +
							"§7никнейм приглашенного!"
					).onClick((click, index, button) -> click.performCommand("invite")),
			new Button()
					.texture("minecraft:textures/items/golden_carrot.png")
					.title("§bОсобое снаряжение")
					.description("§7Тут вы можете купить,\n" +
							"§7интересные вещи..."
					).onClick((click, index, button) -> click.performCommand("donate")),
			new Button()
					.texture("minecraft:textures/items/chest_minecart.png")
					.title("§bЗаказать товар §e500$")
					.description("§7Закажите фургон с продовольствием,\n" +
							"§7он будет вас ждать слева от музея, \n" +
							"§7идите к желтому знаку за тем\n" +
							"§7отнесите товар в лавку."
					).onClick((click, index, button) -> click.performCommand("wagonbuy")),
			new Button()
					.texture("minecraft:textures/other/new_lvl_rare_close.png")
					.title("§bЛутбокс")
					.description("§7㧩 Вы получите случайный" +
							"§7драгоценный камень [60%-100%]," +
							"§7а так же случайный" +
							"§7метеорит доходом от 15$ до 100$."
					).onClick((click, index, button) -> click.performCommand("lootbox"))
	));

	private String cmdMenu(Player player, String[] args) {
		val menu = new Selection(
				"Главное меню",
				"",
				"Открыть",
				2,
				2,
				new Button()
		);
		menu.setStorage(menuButtons);
		menu.open(player);
		return null;
	}

	private static final int PRICE = 25000;
	private static final double CHANCE = 0.40;

	private final List<Button> polishingButton = new ArrayList<>(Collections.singletonList(
			new Button()
					.material(Material.ANVIL)
					.title("§bЮвелир")
					.description("§7Вы можете поменять\n" +
							"§7процент драгоценного\n" +
							"§7камня (от 0 до 110%).\n" +
							"§cШанс потерять камень " + (int) (CHANCE * 100) + "%\n" +
							"§eСтоимость услуги " + MessageUtil.toMoneyFormat(PRICE)
					).onClick((click, index, button) -> {
						User user = App.getApp().getUser(click);
						ItemStack itemInHand = click.getItemInHand();
						NBTTagCompound tag = CraftItemStack.asNMSCopy(itemInHand).tag;
						Gem gem = null;
						for (Fragment currentRelic : user.getRelics().values()) {
							if (currentRelic.getUuid().toString().equals(tag.getString("relic-uuid"))) {
								gem = (Gem) currentRelic;
							}
						}
						if (user.getMoney() >= PRICE && gem != null) {
							click.getInventory().removeItem(click.getItemInHand());
							gem.remove(user);
							user.giveMoney(-PRICE);
							click.closeInventory();
							if (Math.random() < CHANCE)
								Anime.topMessage(user.handle(), "§cКамень был разрушен");
							else {
								new Gem(gem.getType().name() + ":" + Math.random() + ":" + gem.getPrice()).give(user);
								Anime.topMessage(user.handle(), "§aКамень был отполирован");
							}
						} else
							AnimationUtil.buyFailure(user);
					})
	));

	private String cmdPolishing(Player player, String[] args) {
		val menu = new Selection(
				"Полировка",
				"",
				"Полировать",
				1,
				1,
				new Button()
		);
		menu.setStorage(polishingButton);
		menu.open(player);
		return null;
	}

	private final List<Button> lootBoxButtons = new ArrayList<>(Collections.singletonList(
			new Button()
					.texture("minecraft:textures/other/new_lvl_rare_close.png")
					.price(39)
					.title("§bСлучайная посылка")
					.description("§7㧩 Вы получите случайный", "§7драгоценный камень и метеорит!")
					.onClick((click, index, button) -> click.performCommand("proccessdonate ITEM_CASE"))
	));

	private String cmdLootBox(Player player, String[] args) {
		GetAccountBalancePackage money;
		try {
			money = (GetAccountBalancePackage) ISocketClient.get().writeAndAwaitResponse(new GetAccountBalancePackage(player.getUniqueId())).get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
		val menu = new Selection(
				"Лутбокс",
				"Кристаликов " + money.getBalanceData().getCrystals(),
				"Открыть",
				1,
				1,
				new Button()
		);
		menu.setVault("donate");
		menu.setStorage(lootBoxButtons);
		menu.open(player);
		return null;
	}


	private final List<Button> donateButtons = new ArrayList<>(Arrays.asList(
			new Button()
					.material(Material.END_CRYSTAL)
					.price(49)
					.title("§aСлучайный префикс")
					.description("Если такой префикс уже был?\n" +
							"- §eВы получите §6§l50`000 $\n" +
							"Каждое §dпятое §fоткрытие §dгарантирует\n" +
							"§6редкий §fили §dэпичный §fпрефикс"
					).onClick((click, index, button) -> click.performCommand("proccessdonate PREFIX_CASE")),
			new Button()
					.material(Material.EXP_BOTTLE)
					.price(149)
					.title("§bГлобальный бустер опыта §6§lx2")
					.description("Общий бустер на §b1 час§f,", "все получат в два раза больше опыта!")
					.onClick((click, index, button) -> click.performCommand("proccessdonate GLOBAL_EXP_BOOSTER")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
					.price(79)
					.title("§aГлобальный бустер бура §6§lx2")
					.description("Общий бустер на §b1 час§f,\n" +
							"в §lДВА§f раза быстрее работает\n" +
							"бур!"
					).onClick((click, index, button) -> click.performCommand("proccessdonate BOER")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
					.price(149)
					.title("§aБустер посетителей §6§lx3")
					.description("Общий бустер на §b1 час§f,\n" +
							"в §lТРИ§f раза больше посетителей\n" +
							"и §e§lмонет§f!"
					).onClick((click, index, button) -> click.performCommand("proccessdonate GLOBAL_VILLAGER_BOOSTER")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
					.price(199)
					.title("§aГлобальный бустер бура §6§lx5")
					.description("Общий бустер на §b1 час§f,\n" +
							"в §lПЯТЬ§f раза быстрее работает\n" +
							"бур!"
					).onClick((click, index, button) -> click.performCommand("proccessdonate BIG_BOER")),
			new Button()
					.material(Material.GOLDEN_APPLE)
					.price(199)
					.title("§eГлобальный бустер денег §6§lx2")
					.description("Общий бустер на §b1 час§f,\n" +
							"все получат в два раза больше денег!"
					).onClick((click, index, button) -> click.performCommand("proccessdonate GLOBAL_MONEY_BOOSTER")),
			new Button()
					.item(Items.builder().type(Material.GOLDEN_APPLE).enchantment(Enchantment.LUCK, 1).build())
					.price(119)
					.title("§aКомиссия 0%")
					.description("Если вы §aпродаете или покупаете\n" +
							"драгоценный камень, комиссия\n" +
							"§aисчезнет§f, поэтому вы не теряете\n" +
							"денег на переводах валюты."
					).onClick((click, index, button) -> click.performCommand("proccessdonate PRIVILEGES")),
			new Button()
					.texture("minecraft:textures/prison/shoveld.png")
					.price(349)
					.title("§b§lЛегендарная кирка")
					.description("Особая кирка, приносит" +
							"§b2 опыта за блок§f и" +
							"вскапывает §bбольше всех" +
							"других!" +
							"§7Не остается после вайпа"
					).onClick((click, index, button) -> click.performCommand("proccessdonate LEGENDARY_PICKAXE")),
			new Button()
					.texture("minecraft:textures/museum/porovoz.png")
					.price(249)
					.title("§6Стим-панк сборщик монет")
					.description("§bБыстрее всех§f! Собирает самые\n" +
							"дальние монеты -§b лучший выбор\n" +
							"среди коллекторов.\n" +
							"\n" +
							"§7Не остается после вайпа"
					).onClick((click, index, button) -> click.performCommand("proccessdonate STEAM_PUNK_COLLECTOR"))
	));

	private String cmdDonate(Player player, String[] args) {
		GetAccountBalancePackage money;
		try {
			money = (GetAccountBalancePackage) ISocketClient.get().writeAndAwaitResponse(new GetAccountBalancePackage(player.getUniqueId())).get(1, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}
		val menu = new Selection(
				"Донат",
				"Кристаликов " + money.getBalanceData().getCrystals(),
				"Купить",
				2,
				2,
				new Button()
		);
		menu.setVault("donate");
		menu.setStorage(donateButtons);
		menu.open(player);
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

		Glow.animate(user.handle(), 0.3, GlowColor.GREEN);
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
		if (user.getExperience() < PreparePlayerBrain.EXPERIENCE)
			return Formatting.error("Сначала нужно завершить туториал!");
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

	private String cmdResourcePack(Player player, String[] args) {
		player.setResourcePack(System.getenv("RESOURCE_PACK"), "666");
		return Formatting.fine("Установка...");
	}

	private String cmdRate(Player player, String[] args) {
		if (args.length == 0)
			return Formatting.error("/rate <цена>");
		else if (!app.getPlayerDataManager().isRateBegun())
			return Formatting.error("Торги ещё не начались.");
		else if (Integer.parseInt(args[0]) < 10)
			return Formatting.error("Сумма должна быть больше 10$.");
		else if (Integer.parseInt(args[0]) > app.getUser(player).getMoney())
			return Formatting.error("У вас нет таких денег.");
		else
			App.getApp().getPlayerDataManager().getMembers().put(player.getUniqueId(), Integer.parseInt(args[0]));

		return Formatting.fine("Ваша ставка была принята.");
	}
}
