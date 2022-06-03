package museum.command;

import clepto.bukkit.B;
import lombok.val;
import me.func.mod.Anime;
import me.func.mod.Glow;
import me.func.mod.selection.Button;
import me.func.mod.selection.Choicer;
import me.func.mod.selection.Confirmation;
import me.func.mod.selection.Selection;
import me.func.protocol.GlowColor;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.content.PrefixType;
import museum.cosmos.Cosmos;
import museum.cosmos.boer.Boer;
import museum.cosmos.boer.BoerType;
import museum.data.MuseumInfo;
import museum.data.PickaxeType;
import museum.data.SubjectInfo;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.fragment.Fragment;
import museum.fragment.Gem;
import museum.fragment.GemType;
import museum.misc.PlacesMechanic;
import museum.museum.Museum;
import museum.museum.map.SubjectPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.Subject;
import museum.player.User;
import museum.player.pickaxe.PickaxeUpgrade;
import museum.player.prepare.PreparePlayerBrain;
import museum.prototype.Managers;
import museum.util.ItemUtil;
import museum.util.MessageUtil;
import museum.util.VirtualSign;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Formatting;
import ru.cristalix.core.item.Items;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static museum.App.RESOURCE_PACK_URL;

public class MuseumCommands {

	private final App app;
	private static final String PLAYER_OFFLINE_MESSAGE = MessageUtil.get("playeroffline");

	public MuseumCommands(App app) {
		this.app = app;

		B.regCommand(this::cmdPlayerStats, "playerstats");
		B.regCommand(this::cmdAchievements, "achievements");
		B.regCommand(this::cmdVisitor, "visitor");
		B.regCommand(this::cmdPrefixMenu, "prefixes");
		B.regCommand(this::cmdBoerMenu, "boermenu");
		B.regCommand(this::cmdBoer, "boer");
		B.regCommand(this::cmdExcavationSecondMenu, "excavationsecondmenu");
		B.regCommand(this::cmdExcavationMenu, "excavationmenu");
		B.regCommand(this::cmdUpgradeRod, "upgraderod");
		B.regCommand(this::cmdUpgradePickaxe, "upgradepickaxe");
		B.regCommand(this::cmdFastUpgradePickaxe, "fastupgradepickaxe");
		B.regCommand(this::cmdTools, "tools");
		B.regCommand(this::cmdPolishing, "polishing");
		B.regCommand(this::cmdDonateLootBox, "donatelootbox");
		B.regCommand(this::cmdLootBox, "lootbox");
		B.regCommand(this::cmdMenu, "menu");
		B.regCommand(this::cmdDonate, "donate");

		B.regCommand(this::cmdResourcePack, "resourcepack");
		B.regCommand(this::cmdRate, "rate");
		B.regCommand(this::cmdBuy, "buy");
		B.regCommand(this::cmdGui, "gui");
		B.regCommand(this::cmdTravel, "travel");
		B.regCommand(this::cmdExcavation, "excavation", "exc");
		B.regCommand(this::cmdInvite, "invite");
		B.regCommand(this::cmdChangeTitle, "changetitle");
		B.regCommand(this::cmdShop, "shop", "gallery");
		B.regCommand(this::cmdHome, "home", "leave", "spawn");
	}

	private String cmdPlayerStats(Player player, String[] args) {
		val user = App.getApp().getUser(player);
		MuseumInfo museumInfo = user.getMuseumInfos().get(0);

		val menu = new Selection(
				"Ваша статистика",
				"Кристалликов " + user.getDonateMoney(),
				"",
				1,
				1,
				new Button()
						.texture("minecraft:mcpatcher/cit/others/hub/guild_world.png")
						.title("")
						.description("§6Доход:§f " + MessageUtil.toMoneyFormat(user.getIncome()) + "\n" +
								"§6Монет:§f " + MessageUtil.toMoneyFormat(user.getMoney()) + "\n" +
								"§bКосмических кристаллов:§f " + MessageUtil.toCrystalFormat(user.getCosmoCrystal()) + "\n" +
								"§bУровень:§f " + user.getLevel() + "\n" +
								"§cОпыт:§f " + MessageUtil.toCrystalFormat(user.getExperience()) + "\n" +
								"§bРаскопок:§f " + user.getExcavationCount() + "\n" +
								"§bКирка:§f " + getPickaxeColor(user.getPickaxeType()) + user.getPickaxeType().getName() + "\n\n" +
								"§bНазвание музея:§f " + museumInfo.title + "\n" +
								"§bПосещений музея:§f " + museumInfo.views + "\n" +
								"§bСоздан:§f " + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date(museumInfo.creationDate.getTime())) + "\n" +
								"§bФрагментов:§f " + user.getSkeletons().stream().mapToInt(s -> s.getUnlockedFragments().size()).sum() + "\n" +
								"§bПодобрано монет:§f " + user.getPickedCoinsCount() + "\n")
		);

		menu.setVault("donate");
		menu.open(player);
		return null;
	}

	private String cmdAchievements(Player player, String[] args) {
		val user = App.getApp().getUser(player);

		val menu = new Selection(
				"Найденные места",
				"",
				"",
				5,
				4
		);

		ItemStack claimedPlaceItem = ItemUtil.getAgreeItem();

		for (String placeName : user.getClaimedPlaces()) {
			PlacesMechanic.Place place = PlacesMechanic.getPlaceByTitle(placeName);

			Button btn = new Button()
					.item(claimedPlaceItem)
					.title(place.getTitle())
					.description("§6" + place.getClaimedExp() + " опыта");

			menu.add(btn);
		}

		menu.open(player);
		return null;
	}

	private String cmdVisitor(Player player, String[] args) {
		val user = App.getApp().getUser(player);
		val userMoney = user.getMoney();

		val menu = new Selection(
				"Посещение других музеев",
				"Монет " + MessageUtil.toMoneyFormat(userMoney),
				"",
				5,
				2
		);

		for (User userOnServer : app.getUsers()) {
			if (userOnServer != user) {
				ItemStack userSkull = ItemUtil.getPlayerSkull(userOnServer);
				val userMuseumCost = userOnServer.getIncome() / 2;
				val canVisit = userMoney >= userMuseumCost;

				Button btn = new Button()
						.item(userSkull)
						.price((long) userMuseumCost)
						.title(userOnServer.getName() +
								userOnServer.getLastMuseum().getTitle() +
								" §fФрагментов: " + userOnServer.getSkeletons().stream().mapToInt(s -> s.getUnlockedFragments().size()).sum())
						.hint(canVisit ? "Посетить" : "")
						.onClick((clickUser, button, index) -> {
							if (canVisit) {
								user.performCommand("travel " + userOnServer.getName());
								Anime.close(player);
							}
						});

				menu.add(btn);
			}
		}

		menu.open(player);
		return null;
	}

	private String cmdPrefixMenu(Player player, String[] args) {
		val user = App.app.getUser(player);

		val menu = new Selection(
				"Префиксы",
				"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
				"",
				3,
				3
		);

		for (PrefixType prefixType : PrefixType.values()) {
			val prefixRarity = prefixType.getRare();
			val prefixHasBonus = !Objects.equals(prefixType.getBonus(), "");
			val playerHasThisPrefix = user.getPrefixes().contains(prefixType.getPrefix());

			Button btn = new Button()
					.material(prefixRarity == 3 ? Material.EMERALD : prefixRarity == 2 ? Material.GOLD_INGOT : Material.IRON_INGOT)
					.title(prefixHasBonus ? prefixType.getPrefix() + " " + prefixType.getTitle() : prefixType.getPrefix())
					.description(prefixHasBonus ? prefixType.getBonus() : "\n" + prefixType.getTitle())
					.hint(playerHasThisPrefix ? "Поставить" : "")
					.onClick((clickUser, index, button) -> {
						if (playerHasThisPrefix) {
							user.setPrefix(prefixType.getPrefix());
							Anime.close(player);
							Anime.title(player, "Вы §aуспешно§f выбрали префикс " + prefixType.getPrefix());
						}
					});
			menu.add(btn);
		}

		menu.open(player);
		return null;
	}

	private String cmdBoerMenu(Player player, String[] args) {
		val user = App.app.getUser(player);

		// Обработка от индивидов которые достанут uuid бура и пихнут его в команду
		if (args.length != 1) return null;
		for (Fragment value : user.getRelics().values())
			if (value instanceof Boer) {
				if (value.getUuid().toString().equals(args[0]) && ((Boer) value).getOwner().toString().equals(player.getUniqueId().toString())) {
					Boer boer = (Boer) value;
					BoerType nextBoer = boer.getType().getNext() != null ? boer.getType().getNext() : null;

					ItemStack boerItem = new ItemStack(Material.CLAY_BALL);
					val nmsItem = CraftItemStack.asNMSCopy(boerItem);
					nmsItem.tag = new NBTTagCompound();
					nmsItem.tag.setString("other", "win2");
					boerItem = nmsItem.asBukkitMirror();
					ItemStack finalBoerItem = boerItem;

					val menu = new Selection(
							"Настройка бура",
							"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
							"",
							2,
							3,
							new Button()
									.texture("minecraft:mcpatcher/cit/others/badges/info1.png")
									.title("Уведомления")
									.description("\n" + (boer.isNotification() ? "§cВыключить" : "§aВключить") + " §fуведомления от бура!")
									.hint("ℹ")
									.onClick((clickUser, index, button) -> {
										boer.setNotification(!boer.isNotification());
										Anime.close(player);
									}),
							new Button()
									.texture("minecraft:mcpatcher/cit/others/badges/upgrade.png")
									.title("Улучшить бур")
									.description(nextBoer != null
											? "Подробнее"
											: "Данный бур §cмаксимального§b уровня, поздравляем!")
									.hint(nextBoer != null ? "Улучшить" : "")
									.onClick((clickUser, index, button) -> {
										if (nextBoer != null) {
											val new_menu = new Selection(
													"Улучшение бура",
													"бабки",
													"Улучшить",
													1,
													1,
													new Button()
															.texture("minecraft:mcpatcher/cit/others/badges/upgrade.png")
															.title("Улучшение")
															.description("\n§b" + boer.getType().getAddress() + "§f ➠ §c" + nextBoer.getAddress() +
																	"\nСледующий бур работает §b" + nextBoer.getTime() / 3600 +
																	" часа§f и приносит §b1 опыт §fи §b1 кристалл§f\nкаждые §l§b" + nextBoer.getSpeed() + " §fсекунд.\n\n" +
																	"Стоимость: §e" + MessageUtil.toMoneyFormat(nextBoer.getPrice()) + "§f и " + "§b10.000§f кристаллов")
															.onClick((clickUserFromNewMenu, indexFromNewMenu, buttonFromNewMenu) -> {
																User museumUser = app.getUser(clickUserFromNewMenu);

																if (checkNotEnoughMoney(museumUser, (long) nextBoer.getPrice())) {
																	return;
																}

																user.giveMoney(-nextBoer.getPrice());
																boer.setType(nextBoer);
																if (boer.isStanding()) {
																	boer.boerRemove();
																}

																Anime.itemTitle(clickUserFromNewMenu,
																		finalBoerItem,
																		"§aУспешно!",
																		"§bВы перешли на §c" + boer.getType().getAddress() + "§b уровень!",
																		2.0);
																Glow.animate(clickUserFromNewMenu, 2.0, GlowColor.GOLD);
																Anime.close(player);
															})
											);

											new_menu.open(player);
										}
									}),
							new Button()
									.texture("minecraft:mcpatcher/cit/others/badges/remove.png")
									.title("Убрать бур")
									.description("\nСпрятать бур к вам в инвентарь.")
									.hint("❌")
									.onClick((clickUser, index, button) -> {
										boer.boerRemove();
										Anime.close(player);
									})
					);

					B.postpone(1, () -> menu.open(player));
				}
			}
		return null;
	}

	private String cmdBoer(Player player, String[] args) {
		val user = App.app.getUser(player);

		boolean maxCountOfBoers = false;
		int countOfBoers = 0;
		for (Fragment value : user.getRelics().values())
			if (value instanceof Boer)
				++countOfBoers;
		if (countOfBoers == 6) maxCountOfBoers = true;
		boolean finalMaxCountOfBoers = maxCountOfBoers;

		ItemStack boer = new ItemStack(Material.CLAY_BALL);
		val nmsItem = CraftItemStack.asNMSCopy(boer);
		nmsItem.tag = new NBTTagCompound();
		nmsItem.tag.setString("other", "win2");
		boer = nmsItem.asBukkitMirror();
		ItemStack finalBoer = boer;

		val menu = new Selection(
				"Буры",
				"Кристаллов/Монет " + MessageUtil.toCrystalFormat(user.getCosmoCrystal()) + "/" + MessageUtil.toMoneyFormat(user.getMoney()),
				"",
				1,
				1,
				new Button()
						.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
						.hint(maxCountOfBoers ? "" : "Купить")
						.title("&bКосмический бур")
						.description("\n\nДанный бур работает §b1 час§f и приносит §b1 опыт §fи §b1 кристалл§f\nкаждые §l§b" +
								BoerType.STANDARD.getSpeed() + " §fсекунд.\n\nУ вас есть: " + countOfBoers + " из 6-ти буров.\n\n" +
								"Стоимость: §e" + MessageUtil.toMoneyFormat(10000000) + "§f и " + "§b10.000§f кристаллов")
						.onClick((clickUser, index, button) -> {
							if (!finalMaxCountOfBoers) {
								if (user.getMoney() > 10000000 && user.getCosmoCrystal() > 10000) {
									user.giveMoney(-10000000);
									user.giveCosmoCrystal(-10000, false);
									new Boer("boer_" + BoerType.STANDARD.name(), player.getUniqueId()).give(user);
									Anime.close(player);
									Anime.itemTitle(clickUser,
											finalBoer,
											"§aУспешно!",
											"§bВы приобрели §c" + BoerType.STANDARD.getAddress() + "§b бур 1 уровня!",
											2.0);
									Glow.animate(clickUser, 2.0, GlowColor.GOLD);
								} else {
									AnimationUtil.buyFailure(user);
								}
							}
						})
		);

		B.postpone(1, () -> menu.open(player));
		return null;
	}

	private String cmdExcavationSecondMenu(Player player, String[] args) {
		User museumUser = app.getUser(player);

		val menu = new Selection(
				"Экспедиции",
				"",
				"Путешествие",
				4,
				2
		);

		val excavations = Managers.excavation.stream().sorted(Comparator.comparing(ExcavationPrototype::getPrice)).collect(Collectors.toList());

		for (ExcavationPrototype exc : excavations) {
			Button btnMapUnlocked = new Button()
					.item(exc.getIcon())
					.title(exc.getTitle())
					.description("Цена отправления: " + MessageUtil.toMoneyFormat(exc.getPrice()))
					.onClick((clickUser, index, button) -> {
						if (museumUser.getMoney() > exc.getPrice()) {
							clickUser.performCommand("excavation " + exc.getAddress());
						} else {
							AnimationUtil.buyFailure(museumUser);
						}
					});

			Button btnMapLocked = new Button()
					.texture("minecraft:mcpatcher/cit/others/lock.png")
					.title("§cЗакрыто")
					.description("§7Необходимый уровень: " + exc.getRequiredLevel())
					.hint("");

			if (museumUser.getLevel() >= exc.getRequiredLevel()) {
				menu.add(btnMapUnlocked);
			} else {
				menu.add(btnMapLocked);
			}

		}
		menu.open(player);
		return null;
	}

	private String cmdExcavationMenu(Player player, String[] args) {
		GemType dailyCave = GemType.getActualGem();
		val dailyGem = new Gem(dailyCave.name() + ':' + 1.0 + ":10000").getItem();

		Choicer menu = new Choicer(
				"Экспедиции",
				"Выбирайте куда отправиться!",
				new Button()
						.material(Material.NETHER_STAR)
						.title("Рынок")
						.description("Обменивайтесь\n §cдрагоценными\n§cкамнями§f с другими\n игроками!")
						.hint("Путешествие")
						.onClick((clickUser, index, button) -> {
							clickUser.performCommand("shop poly");
							Anime.close(player);
						}),
				new Button()
						.texture("minecraft:mcpatcher/cit/others/blocks.png")
						.title("Раскопки")
						.description("Познавайте\n различные дыры!")
						.hint("Путешествие")
						.onClick((clickUser, index, button) -> clickUser.performCommand("excavationsecondmenu")),
				new Button()
						.item(dailyGem)
						.title("§b" + dailyCave.getDayTag() + "§f " + dailyCave.getLocation())
						.description("Там добываются\n" + dailyCave.getTitle())
						.hint("Путешествие")
						.onClick((clickUser, index, button) -> {
							App.app.getUser(clickUser).setState(App.app.getCrystal());
							Anime.close(player);
						}),
				new Button()
						.item(ItemUtil.cosmoCrystal())
						.title("Космос")
						.description("Вы когда нибудь\n хотели побывать\n в космосе?")
						.hint("Путешествие")
						.onClick((clickUser, index, button) -> {
							Anime.close(player);
							player.teleport(Cosmos.ROCKET);
						})
		);

		B.postpone(1, () -> menu.open(player));
		return null;
	}

	private String cmdUpgradeRod(Player player, String[] args) {
		val user = App.app.getUser(player);

		long cost = 0;
		switch (user.getInfo().getHookLevel()) {
			case 1:
				cost = 30000;
				break;
			case 2:
				cost = 1000000;
				break;
			case 3:
				cost = 3000000;
				break;
			case 4:
				break;
		}
		long finalCost = cost;

		val menu = new Selection(
				"Улучшение крюка",
				"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
				"",
				1,
				1,
				new Button()
						.material(Material.FISHING_ROD)
						.price(cost)
						.hint(user.getHookLevel() == 4 ? "" : "Улучшить")
						.title(user.getHookLevel() == 4 ? "§fУ вас крюк §bмаксимального §fуровня" : "§fКрюк УР. §b" + user.getHookLevel())
						.description("")
						.onClick((clickPlayer, index, button) -> {
							if (user.getHookLevel() != 4) {
								val clickUser = App.getApp().getUser(clickPlayer);

								if (checkNotEnoughMoney(clickUser, finalCost)) {
									return;
								}

								clickUser.giveMoney(-finalCost);
								clickUser.setHookLevel(clickUser.getHookLevel() + 1);
								Anime.close(player);

								Anime.itemTitle(clickPlayer,
										Items.builder().type(Material.FISHING_ROD).enchantment(Enchantment.LUCK, 1).build(),
										"§aУспешно!",
										"§bВы улучшили крюк до " + clickUser.getHookLevel() + " §bуровня!",
										2.0);
								Glow.animate(clickPlayer, 2.0, GlowColor.GOLD);
							}
						})
		);
		menu.open(player);
		return null;
	}

	private String cmdUpgradePickaxe(Player player, String[] args) {
		val user = App.app.getUser(player);

		val userHaveLastPickaxe = user.getPickaxeType().equals(PickaxeType.LEGENDARY);
		val userPickaxeType = user.getPickaxeType();

		long cost = 0;
		switch (userPickaxeType) {
			case DEFAULT:
				cost = 50000;
				break;
			case PROFESSIONAL:
				cost = 3000000;
				break;
			case PRESTIGE:
				cost = 100000000;
				break;
			case LEGENDARY:
				break;
		}
		long finalPickaxeUpgradeTypeCost = cost;

		val menu = new Selection(
				"Улучшения кирки",
				"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
				"",
				3,
				3,
				new Button()
						.item(getPickaxeImage(userPickaxeType))
						.price(finalPickaxeUpgradeTypeCost)
						.hint(userHaveLastPickaxe ? "" : "Улучшить")
						.title("§fУ вас:")
						.description(getPickaxeColor(userPickaxeType) + userPickaxeType.getName() + " §fкирка.")
						.onClick((clickPlayer, index, button) -> {
							if (!userHaveLastPickaxe) {
								val clickUser = App.getApp().getUser(clickPlayer);

								if (checkNotEnoughMoney(clickUser, finalPickaxeUpgradeTypeCost)) {
									return;
								}

								clickUser.giveMoney(-finalPickaxeUpgradeTypeCost);
								clickUser.setPickaxeType(userPickaxeType.getNext());
								Anime.close(player);

								Anime.itemTitle(clickPlayer,
										getPickaxeImage(clickUser.getPickaxeType()),
										"§aУспешно!",
										"§bТеперь вы владеете\n" + getPickaxeColor(clickUser.getPickaxeType()) + clickUser.getPickaxeType().getName().replace("ая", "ой") + "\n§bкиркой!",
										2.0);
								Glow.animate(clickPlayer, 2.0, GlowColor.GOLD);
							}
						})
		);

		int indexOfElement = 0;
		for (PickaxeUpgrade pickaxeUpgrade : PickaxeUpgrade.values()) {
			int finalIndexOfElement = indexOfElement;
			int currentLevelOfUpgrade = (int) (pickaxeUpgrade.convert(user) / pickaxeUpgrade.getStep());
			boolean userHaveMaxLevelOfUpgrade = currentLevelOfUpgrade == pickaxeUpgrade.getMaxLevel();

			Button btn = new Button()
					.texture("minecraft:mcpatcher/cit/museum/wood_pickaxe_upgrade.png")
					.price(userHaveMaxLevelOfUpgrade ? 0 : pickaxeUpgrade.getCost())
					.hint(userHaveMaxLevelOfUpgrade ? "" : "Улучшить")
					.title(pickaxeUpgrade.getTitle())
					.description("\n" + currentLevelOfUpgrade + "/" + pickaxeUpgrade.getMaxLevel())
					.onClick((clickPlayer, index, button) -> {
						if (!userHaveMaxLevelOfUpgrade) {
							val clickUser = App.getApp().getUser(clickPlayer);
							if (pickaxeUpgrade.getMaxLevel() < 10) {
								upgradePickaxeImprovement(clickUser, clickPlayer, pickaxeUpgrade);
							} else {
								clickUser.performCommand("fastupgradepickaxe " + finalIndexOfElement);
							}
						}
					});

			++indexOfElement;
			menu.add(btn);
		}

		menu.open(player);
		return null;
	}

	private String cmdFastUpgradePickaxe(Player player, String[] args) {
		User user = App.app.getUser(player);
		PickaxeUpgrade pickaxeUpgrade = PickaxeUpgrade.values()[Integer.parseInt(args[0])];

		int countOfMaxUpgrades = user.getMoney() >= pickaxeUpgrade.getCost() * (long) (pickaxeUpgrade.getMaxLevel() - pickaxeUpgrade.convert(user)) ?
				(int) (pickaxeUpgrade.getMaxLevel() - (pickaxeUpgrade.convert(user) / pickaxeUpgrade.getStep())) :
				(int) (user.getMoney() / pickaxeUpgrade.getCost());

		val menu = new Selection(
				"Мгновенное улучшение",
				"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
				"",
				2,
				2,
				new Button()
						.texture("minecraft:mcpatcher/cit/museum/wood_pickaxe_upgrade.png")
						.price(pickaxeUpgrade.getCost())
						.hint("Улучшить")
						.title(pickaxeUpgrade.getTitle())
						.description("\nКупить §a1§f улучшение!")
						.onClick((clickPlayer, index, button) -> {
							val clickUser = App.getApp().getUser(clickPlayer);
							upgradePickaxeImprovement(clickUser, clickPlayer, pickaxeUpgrade);
						}),
				new Button()
						.texture("minecraft:mcpatcher/cit/museum/wood_pickaxe_upgrade.png")
						.price(pickaxeUpgrade.getCost() * 10L)
						.hint("Улучшить")
						.title(pickaxeUpgrade.getTitle())
						.description("Купить сразу §a10§f улучшений!")
						.onClick((clickPlayer, index, button) -> {
							val clickUser = App.getApp().getUser(clickPlayer);
							multiplyUpgradePickaxeImprovement(clickUser, clickPlayer, pickaxeUpgrade, 10L);
						}),
				new Button()
						.texture("minecraft:mcpatcher/cit/museum/wood_pickaxe_upgrade.png")
						.price(pickaxeUpgrade.getCost() * (long) countOfMaxUpgrades)
						.hint("Улучшить")
						.title(pickaxeUpgrade.getTitle())
						.description("Купить §aвсё§f, что вы можете себе позволить!\n\n§cВнимание§f, вы §cприобретаете §a" + countOfMaxUpgrades + "§f улучшений!")
						.onClick((clickPlayer, index, button) -> {
							val clickUser = App.getApp().getUser(clickPlayer);
							multiplyUpgradePickaxeImprovement(clickUser, clickPlayer, pickaxeUpgrade, (long) countOfMaxUpgrades);
						})
		);

		menu.open(player);
		return null;
	}

	private void multiplyUpgradePickaxeImprovement(User museumUser, Player clickPlayer, PickaxeUpgrade pickaxeUpgrade, Long countOfUpgrades) {
		if (checkNotEnoughMoney(museumUser, pickaxeUpgrade.getCost() * countOfUpgrades))
			return;

		if (museumUser.getPickaxeImprovements().get(pickaxeUpgrade) + countOfUpgrades > pickaxeUpgrade.getMaxLevel()) {
			countOfUpgrades = (long) (pickaxeUpgrade.getMaxLevel() - museumUser.getPickaxeImprovements().get(pickaxeUpgrade));
		}
		museumUser.giveMoney(-(pickaxeUpgrade.getCost() * countOfUpgrades));
		museumUser.getPickaxeImprovements().replace(pickaxeUpgrade, museumUser.getPickaxeImprovements().get(pickaxeUpgrade) + countOfUpgrades.intValue());
		Anime.close(clickPlayer);

		ItemStack itemStack = new ItemStack(Material.CLAY_BALL);
		val nmsItem = CraftItemStack.asNMSCopy(itemStack);
		nmsItem.tag = new NBTTagCompound();
		nmsItem.tag.setString("museum", "wood_pickaxe_upgrade");
		itemStack = nmsItem.asBukkitMirror();

		Anime.itemTitle(clickPlayer,
				itemStack,
				"§aУспешно!",
				"§bВы перешли на " + museumUser.getPickaxeImprovements().get(pickaxeUpgrade) + " уровень улучшения\n" +
						pickaxeUpgrade.getTitle(),
				2.0);
		Glow.animate(clickPlayer, 2.0, GlowColor.GREEN);
	}

	private void upgradePickaxeImprovement(User museumUser, Player clickPlayer, PickaxeUpgrade pickaxeUpgrade) {
		if (checkNotEnoughMoney(museumUser, (long) pickaxeUpgrade.getCost())) {
			return;
		}

		museumUser.giveMoney(-pickaxeUpgrade.getCost());
		museumUser.getPickaxeImprovements().replace(pickaxeUpgrade, museumUser.getPickaxeImprovements().get(pickaxeUpgrade) + 1);
		Anime.close(clickPlayer);

		ItemStack itemStack = new ItemStack(Material.CLAY_BALL);
		val nmsItem = CraftItemStack.asNMSCopy(itemStack);
		nmsItem.tag = new NBTTagCompound();
		nmsItem.tag.setString("museum", "wood_pickaxe_upgrade");
		itemStack = nmsItem.asBukkitMirror();

		Anime.itemTitle(clickPlayer,
				itemStack,
				"§aУспешно!",
				"§bВы перешли на " + museumUser.getPickaxeImprovements().get(pickaxeUpgrade) + " уровень улучшения\n" +
						pickaxeUpgrade.getTitle(),
				2.0);
		Glow.animate(clickPlayer, 2.0, GlowColor.GREEN);
	}

	private ItemStack getPickaxeImage(PickaxeType pickaxeType) {
		val pickaxe = clepto.bukkit.item.Items.render(pickaxeType.name().toLowerCase()).asBukkitMirror();
		val meta = pickaxe.getItemMeta();
		meta.addEnchant(Enchantment.DIG_SPEED, meta.getEnchantLevel(Enchantment.DIG_SPEED), true);
		pickaxe.setItemMeta(meta);
		return pickaxe;
	}

	private String getPickaxeColor(PickaxeType pickaxeType) {
		if (pickaxeType.equals(PickaxeType.DEFAULT)) return "§7";
		if (pickaxeType.equals(PickaxeType.PROFESSIONAL)) return "§3";
		if (pickaxeType.equals(PickaxeType.PRESTIGE)) return "§6";
		if (pickaxeType.equals(PickaxeType.LEGENDARY)) return "§b";
		return "";
	}

	private boolean checkNotEnoughMoney(User clickUser, Long finalCost) {
		if (clickUser.getMoney() < finalCost) {
			AnimationUtil.buyFailure(clickUser);
			return true;
		} else {
			return false;
		}
	}

	private String cmdTools(Player player, String[] args) {
		val menu = new Selection(
				"Меню инструментов",
				"",
				"Открыть",
				2,
				2,
				new Button()
						.texture("minecraft:textures/items/gold_pickaxe.png")
						.title("§bКирки")
						.description("Приобретите новую кирку и разгадайте тайны песка...")
						.onClick((click, index, button) -> click.performCommand("upgradepickaxe")),
				new Button()
						.texture("minecraft:textures/items/fishing_rod_uncast.png")
						.title("§bКрюк")
						.description("Улучшайте крюк, чтобы быстрее получать опыт на реке международных раскопок кристаллов.")
						.onClick((click, index, button) -> click.performCommand("upgraderod"))
		);
		menu.open(player);
		return null;
	}

	private static final int PRICE = 25000;
	private static final double CHANCE = 0.40;

	private String cmdPolishing(Player player, String[] args) {
		User user = App.getApp().getUser(player);

		val menu = new Selection(
				"Полировка",
				"Монет " + MessageUtil.toMoneyFormat(user.getMoney()),
				"Полировать",
				1,
				1,
				new Button()
						.texture("minecraft:mcpatcher/cit/others/anvil.png")
						.title("§bЮвелир")
						.description("§7Вы можете поменять\n" +
								"§7процент драгоценного\n" +
								"§7камня (от 0 до 110%).\n" +
								"§cШанс потерять камень " + (int) (CHANCE * 100) + "%\n" +
								"§eСтоимость услуги " + MessageUtil.toMoneyFormat(PRICE)
						).onClick((click, index, button) -> {
							User clickUser = App.getApp().getUser(click);
							ItemStack itemInHand = click.getItemInHand();
							NBTTagCompound tag = CraftItemStack.asNMSCopy(itemInHand).tag;
							Gem gem = null;
							for (Fragment currentRelic : clickUser.getRelics().values()) {
								if (currentRelic.getUuid().toString().equals(tag.getString("relic-uuid"))) {
									gem = (Gem) currentRelic;
								}
							}
							if (clickUser.getMoney() >= PRICE && gem != null) {
								click.getInventory().removeItem(click.getItemInHand());
								gem.remove(clickUser);
								clickUser.giveMoney(-PRICE);
								Anime.close(player);
								if (Math.random() < CHANCE)
									Anime.topMessage(clickUser.handle(), "§cКамень был разрушен");
								else {
									new Gem(gem.getType().name() + ":" + Math.random() + ":" + gem.getPrice()).give(clickUser);
									Anime.topMessage(clickUser.handle(), "§aКамень был отполирован");
								}
							} else
								AnimationUtil.buyFailure(clickUser);
						})
		);
		B.postpone(1, () -> menu.open(player));
		return null;
	}

	private String cmdDonateLootBox(Player player, String[] args) {
		val user = app.getUser(player);
		int SALE_PREFIX_LOOTBOX = 25;
		int SALE_LOOTBOX = 50;

		val menu = new Selection(
				"Донатные лутбоксы",
				"Кристаликов " + user.getDonateMoney(),
				"Открыть",
				2,
				2,
				new Button()
						.material(Material.END_CRYSTAL)
						.price(49)
						.sale(SALE_PREFIX_LOOTBOX)
						.title("§aСлучайный префикс")
						.description("Такой префикс уже был?\n" +
								"§eВы получите §6§l50,000$\n\n" +
								"Каждое §dпятое §fоткрытие §dгарантирует §6редкий §fили\n" +
								"§dэпичный §fпрефикс.")
						.onClick((click, index, button) -> {
							Confirmation menuConfirm = new Confirmation(Arrays.asList("Купить §aСлучайный префикс", "за &b" + (49 - (49 * SALE_PREFIX_LOOTBOX / 100)) + " кристаллика(ов)"),
									clickPlayer -> clickPlayer.performCommand("proccessdonate PREFIX_CASE"));
							menuConfirm.open(click);
						}),
				new Button()
						.material(Material.ENDER_CHEST)
						.price(78)
						.sale(SALE_LOOTBOX)
						.title("Случайная посылка")
						.description("Вы §dгарантированно §fполучите случайный драгоценный камень 60%-100% качества и " +
								"метеорит c доходом от 15$ до 100$")
						.onClick((click, index, button) -> {
							Confirmation menuConfirm = new Confirmation(Arrays.asList("Купить §aСлучайный префикс", "за &b" + (78 - (78 * SALE_LOOTBOX / 100)) + " кристаллика(ов)"),
									clickPlayer -> clickPlayer.performCommand("proccessdonate ITEM_CASE"));
							menuConfirm.open(click);
						})
		);
		menu.setVault("donate");
		B.postpone(1, () -> menu.open(player));
		return null;
	}

	private String cmdLootBox(Player player, String[] args) {
		val user = app.getUser(player);
		val userHaveEnoughMoney = user.getMoney() >= 10000000;

		val menu = new Selection(
				"Лутбоксы",
				"Монет " + user.getMoney(),
				"",
				2,
				2,
				new Button()
						.material(Material.END_CRYSTAL)
						.price(10000000)
						.hint(userHaveEnoughMoney ? "Открыть" : "")
						.title("§aСлучайный префикс")
						.description("Такой префикс уже был?\n" +
								"§eВы получите §6§l50,000$\n\n" +
								"Каждое §dпятое §fоткрытие §dгарантирует §6редкий §fили\n" +
								"§dэпичный §fпрефикс.")
						.onClick((clickPlayer, index, button) -> {
							if (userHaveEnoughMoney) {
								Anime.close(clickPlayer);
								clickPlayer.performCommand("prefixbox");
							}
						}),
				new Button()
						.material(Material.ENDER_CHEST)
						.price(10000000)
						.hint(userHaveEnoughMoney ? "Открыть" : "")
						.title("Случайная посылка")
						.description("Вы §dгарантированно §fполучите случайный драгоценный камень 60%-100% качества и " +
								"метеорит c доходом от 15$ до 100$")
						.onClick((clickPlayer, index, button) -> {
							if (userHaveEnoughMoney) {
								Anime.close(clickPlayer);
								clickPlayer.performCommand("lootboxopen");
							}
						})
		);
		B.postpone(1, () -> menu.open(player));
		return null;
	}

	private final List<Button> menuButtons = new ArrayList<>(Arrays.asList(
			new Button()
					.texture("minecraft:textures/items/sign.png")
					.title("Переименовать музей")
					.description("Изменить название вашего музея")
					.onClick((click, index, button) -> click.performCommand("changetitle")),
			new Button()
					.texture("minecraft:textures/items/end_crystal.png")
					.title("Префиксы")
					.description("Символ перед ником, некоторые дают особые способности")
					.onClick((click, index, button) -> click.performCommand("prefixes")),
			new Button()
					.material(Material.ENDER_PEARL)
					.title("День / Ночь")
					.description("Меняйте режим так, как будет приятно вашим глазам")
					.onClick((click, index, button) -> {
						val user = App.getApp().getUser(click);
						user.getPlayer().setPlayerTime(user.getInfo().isDarkTheme() ? 12000 : 21000, true);
						user.getInfo().setDarkTheme(!user.getInfo().isDarkTheme());
						Anime.close(click);
					}),
			new Button()
					.texture("minecraft:textures/items/gold_pickaxe.png")
					.title("Инструменты")
					.description("Улучшайте ваше снаряжение")
					.onClick((click, index, button) -> click.performCommand("tools")),
			new Button()
					.texture("minecraft:textures/items/compass_00.png")
					.title("Экспедиции")
					.description("Исследования! Реликвии! Метеориты! Останки динозавров!")
					.onClick((click, index, button) -> click.performCommand("excavationmenu")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/guild_shop.png")
					.title("Магазин")
					.description("Отправляйтесь за новыми постройками")
					.onClick((click, index, button) -> click.performCommand("shop")),
			new Button()
					.item(Items.builder().type(Material.GOLDEN_CARROT).enchantment(Enchantment.LUCK, 1).build())
					.title("§bДонат")
					.description("Различные плюшки")
					.onClick((click, index, button) -> click.performCommand("donate")),
			new Button()
					.texture("minecraft:textures/items/minecart_chest.png")
					.title("Заказать товар §e500$")
					.description("Заберите товар у фуры и продавайте его в лавке")
					.onClick((click, index, button) -> click.performCommand("wagonbuy")),
			new Button()
					.texture("minecraft:textures/items/book_writable.png")
					.title("Пригласить друга")
					.description("Нажмите и введите никнейм приглашенного друга")
					.onClick((click, index, button) -> click.performCommand("invite")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/new_lvl_rare_close.png")
					.title("Донатные лутбоксы")
					.description("Купите шанс получить топовые предметы")
					.onClick((click, index, button) -> click.performCommand("donatelootbox")),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/new_lvl_rare_close.png")
					.title("Лутбоксы")
					.description("Купите шанс получить топовые предметы")
					.onClick((click, index, button) -> click.performCommand("lootbox"))
	));

	private String cmdMenu(Player player, String[] args) {
		val menu = new Selection(
				"Главное меню",
				"",
				"Открыть",
				3,
				3,
				new Button()
		);
		menu.setStorage(menuButtons);
		menu.open(player);
		return null;
	}

	private static final int DONATE_SALE = 25;
	private final List<Button> donateButtons = new ArrayList<>(Arrays.asList(
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/coin2.png")
					.price(119)
					.title("§6Комиссия 0%")
					.description("Если вы §aпродаете или покупаете §fдрагоценный камень, то комиссия " +
							"§aисчезнет§f, поэтому вы не теряете денег на переводах валюты.")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §6Комиссия 0%", "за &b" + (119 - (119 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate PRIVILEGES"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.item(getPickaxeImage(PickaxeType.LEGENDARY))
					.price(349)
					.title("§b§lЛегендарная кирка")
					.description("Особая кирка!\n\n" +
							"Приносит §b2 опыта за вскапывание §fи добывает §bбольше §fвсех других!" +
							"\n\n§cНе остается §fпосле вайпа")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §b§lЛегендарная кирка", "за &b" + (349 - (349 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate LEGENDARY_PICKAXE"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.material(Material.OBSERVER)
					.price(249)
					.title("§6Стим-панк сборщик монет")
					.description("Быстрее всех§f!\n\n" +
							"Собирает самые дальние монеты -§b лучший выбор среди коллекторов." +
							"\n\n§cНе остается §fпосле вайпа")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §6Стим-панк сборщик монет", "за &b" + (249 - (249 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate STEAM_PUNK_COLLECTOR"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.material(Material.EXP_BOTTLE)
					.price(149)
					.title("§bБустер опыта §6§lx2")
					.description("Общий бустер на §b1 час§f.\n\n" +
							"Все получат в §lДВА§f раза больше опыта!")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §bБустер опыта §6§lx2", "за &b" + (149 - (149 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate GLOBAL_EXP_BOOSTER"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
					.price(79)
					.title("§aБустер бура §6§lx2")
					.description("Общий бустер на §b1 час§f.\n\n" +
							"Бур работает в §lДВА§f раза быстрее у всех!")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §bБустер опыта §6§lx2", "за &b" + (79 - (79 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate BOER"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/hub/iconpack/win2.png")
					.price(199)
					.title("§aБустер бура §6§lx5")
					.description("Общий бустер на §b1 час§f.\n\n" +
							"Бур работает в §a§lПЯТЬ§f раз быстрее у всех!")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §bБустер опыта §6§lx5", "за &b" + (199 - (199 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate BIG_BOER"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.texture("minecraft:mcpatcher/cit/others/villager.png")
					.price(149)
					.title("§aБустер посетителей §6§lx3")
					.description("Общий бустер на §b1 час§f.\n\n" +
							"У всех в §lТРИ§f раза больше посетителей и §e§lмонет§f!")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §aБустер посетителей §6§lx3", "за &b" + (149 - (149 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate GLOBAL_VILLAGER_BOOSTER"));
						menu.open(click);
					})
					.sale(DONATE_SALE),
			new Button()
					.material(Material.GOLDEN_APPLE)
					.price(199)
					.title("§eБустер денег §6§lx2")
					.description("Общий бустер на §b1 час§f.\n\n" +
							"Все получат в §lДВА§f раза больше денег!")
					.onClick((click, index, button) -> {
						Confirmation menu = new Confirmation(Arrays.asList("Купить §eБустер денег §6§lx2", "за &b" + (199 - (199 * DONATE_SALE / 100)) + " кристаллика(ов)"),
								player -> player.performCommand("proccessdonate GLOBAL_MONEY_BOOSTER"));
						menu.open(click);
					})
					.sale(DONATE_SALE)
	));

	private String cmdDonate(Player player, String[] args) {
		val user = app.getUser(player);
		val menu = new Selection(
				"Донат",
				"Кристаликов " + user.getDonateMoney(),
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

	private String cmdResourcePack(Player player, String[] args) {
		val resource = System.getenv("RESOURCE_PACK");
		player.setResourcePack(resource == null ? RESOURCE_PACK_URL : resource, "4");
		return null;
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

			visitor.giveMoney(-(museum.getIncome() / 2));
			owner.giveMoney(museum.getIncome() / 2);
			visitor.setState(state);

			ItemStack ownerSkull = ItemUtil.getPlayerSkull(owner);

			Anime.itemTitle(sender,
					ownerSkull,
					"",
					"§bВы посетили музей игрока " + owner.getDisplayName(),
					2.0);

			Anime.killboardMessage(owner.getPlayer(), "§bВаш музей посетили, вы получили &6" + museum.getIncome() / 2 + "$");
		}
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

		Anime.close(player);
		user.setState(new Excavation(prototype, prototype.getHitCount()));
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

	private String cmdShop(Player sender, String[] args) {
		User user = app.getUser(sender);

		if (user.getPlayer() == null)
			return null;

		if (user.getExperience() < PreparePlayerBrain.EXPERIENCE)
			return Formatting.error("Сначала нужно завершить туториал!");

		if (user.getState() instanceof Excavation)
			return MessageUtil.get("museum-first");

		Anime.close(user.handle());

		if (args.length < 1) {
			user.setState(app.getShop());
			return null;
		}

		user.setState(args[0].equals("poly") ? app.getMarket() : app.getShop());
		return null;
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

}
