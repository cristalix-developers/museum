package museum.museum.subject;

import lombok.val;
import lombok.var;
import me.func.mod.Anime;
import me.func.mod.reactive.ReactiveButton;
import me.func.mod.ui.Glow;
import me.func.mod.ui.menu.selection.Selection;
import me.func.protocol.data.color.GlowColor;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.museum.Museum;
import museum.museum.map.SkeletonSubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.BannerUtil;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.formatting.Formatting;
import ru.cristalix.core.item.Items;

import java.util.ArrayList;
import java.util.Comparator;

import static museum.museum.map.SubjectType.SKELETON_CASE;
import static museum.museum.subject.Allocation.Action.*;
import static museum.util.Colorizer.applyColor;

public class SubjectGui {
	public static void showSketelonGui(User user, SkeletonSubject subject) {
		val player = user.getPlayer();
		Anime.close(player);

		val menu = Selection.builder()
				.title(subject.getPrototype().getTitle())
				.hint("Открыть")
				.vault("\uE03F")
				.money(MessageUtil.toMoneyFormat(user.getMoney()))
				.columns(3)
				.rows(3)
				.storage(
						getInformationFragment(subject),
						getSkeletonsListButton(user, subject),
						getColorChangeButton(player, subject),
						getRemoveSubjectButton(player, subject),
						getUpgradeButton(user, subject)
				).build();
		menu.open(player);
	}

	public static void showChangeColorGui(Player player, Subject subject) {
		val buttons = new ArrayList<ReactiveButton>();
		for (val color : Color.values()) {
			val icon = new Items.Builder().type(Material.CONCRETE).color(color).build();

			buttons.add(new ReactiveButton()
					.item(icon)
					.title(color.getTeamName())
					.onClick((click, index, button) -> {
						subject.getCachedInfo().setColor(color);
						val allocation = subject.getAllocation();
						if (allocation != null) {
							allocation.prepareUpdate(data -> applyColor(data, color));
							allocation.perform(Allocation.Action.UPDATE_BLOCKS);
						}
						if (subject instanceof FountainSubject) {
							subject.setAllocation(subject.getAllocation());
						}
					})
			);
		}

		val menu = Selection.builder()
				.title(subject.getPrototype().getTitle())
				.hint("Использовать")
				.rows(4)
				.columns(4)
				.storage(buttons)
				.build();

		menu.open(player);
	}

	public static void showRelicGui(User user, RelicShowcaseSubject subject) {
		val player = user.getPlayer();
		Anime.close(player);

		val buttons = new ArrayList<ReactiveButton>();

		val button = new ReactiveButton();
		if (subject.getFragment() != null) {
			val item = subject.getFragment().getItem();
			StringBuilder description = new StringBuilder();
			if (item.getLore() != null) for (val it : item.getLore()) description.append(it).append("\n");
			button.setItem(item);
			button.setTitle(item.getI18NDisplayName());
			button.setDescription(description.toString());
		} else {
			button.setTexture("minecraft:mcpatcher/cit/others/search.png");
			button.setTitle("Пусто");
			button.setDescription("Возьмите в руку реликвию \nи нажмите ПКМ на стенд\nдля её установки");
		}
		buttons.add(button);

		if (subject.getFragment() != null) {
			buttons.add(new ReactiveButton()
					.texture("minecraft:mcpatcher/cit/others/badges/cancel.png")
					.title("Убрать реликвию")
					.hint("Убрать")
					.onClick((click, index, __) -> removeRelic(user, subject)));
		} else {
			buttons.add(getRemoveSubjectButton(player, subject));
		}

		val menu = Selection.builder()
				.title(subject.getPrototype().getTitle())
				.hint("Информация")
				.rows(2)
				.columns(2)
				.storage(buttons)
				.build();

		menu.open(player);
	}

	public static void getSkeletonListGui(User user, SkeletonSubject subject) {
		val availableButtons = new ArrayList<ReactiveButton>();
		val lockedbuttons = new ArrayList<ReactiveButton>();

		for (val it : Managers.skeleton.stream().sorted(Comparator.comparing(SkeletonPrototype::getTitle)).toArray()) {
			val skeleton = user.getSkeletons().get((SkeletonPrototype) it);

			if (skeleton == null) {
				lockedbuttons.add(new ReactiveButton()
						.title("Не открыто")
						.hint("")
						.texture("minecraft:mcpatcher/cit/others/lock.png"));
				continue;
			}

			val placedOn = user.getMuseums().get(Managers.museum.getPrototype("main")).getSubjects(SKELETON_CASE)
					.stream().filter(item -> (item.getSkeleton() != null && item.getSkeleton() == skeleton)).findFirst();

			val button = new ReactiveButton()
					.title(skeleton.getPrototype().getTitle())
					.description(skeleton.getUnlockedFragments().size() + "/" + skeleton.getPrototype().getFragments().size())
					.texture("minecraft:textures/items/bone.png");

			if (skeleton.getUnlockedFragments().size() < 3) {
				button.setHint("Слишком\nмало");
				button.setHover("Должно быть минимум 3 фрагмента");
			} else if (skeleton.getPrototype().getSize() > ((SkeletonSubjectPrototype) subject.getPrototype()).getSize()) {
				button.setHint("Слишком\nбольшой");
				button.setHover("Скелет cлишком большой для этого стенда");
			} else {
				if (skeleton == subject.getSkeleton()) {
					button.item(new Items.Builder().type(Material.BONE).build());
					button.setTitle("§b" + button.getTitle());
					button.setHint("Убрать");
					button.special(true);
				} else if (placedOn.isPresent() && skeleton == placedOn.get().getSkeleton()) {
					button.setHint("Уже\nиспользуется");
					button.setHover("Уже используется на другом стенде");
				} else {
					button.setHint("Поставить");
				}

				button.onClick((click, index, __) -> {
					Skeleton previousSkeleton = subject.getSkeleton();
					Allocation allocation = subject.getAllocation();

					if (allocation != null) {
						if (previousSkeleton != null) {
							allocation.perform(HIDE_PIECES);
							allocation.removePiece(previousSkeleton.getPrototype());
						}
						if (subject.getSkeleton() == skeleton)
							subject.setSkeleton(null);
						else {
							for (val item : user.getSubjects()) {
								if (item instanceof SkeletonSubject && ((SkeletonSubject) item).getSkeleton() == skeleton) {
									((SkeletonSubject) item).setSkeleton(null);
									((SkeletonSubject) item).updateSkeleton(true);
									if (item.getAllocation() != null) {
										item.getAllocation().perform(HIDE_PIECES);
										item.getAllocation().removePiece(skeleton.getPrototype());
									}
								}
							}
							subject.setSkeleton(skeleton);
						}
						subject.updateSkeleton(true);
						user.updateIncome();
						BannerUtil.updateBanners(subject);
					}

					Anime.close(user.getPlayer());
				});
			}
			availableButtons.add(button);
		}

		val menu = Selection.builder()
				.title("Скелеты")
				.hint("Открыть")
				.rows(5)
				.columns(2)
				.build();

		availableButtons.addAll(lockedbuttons);
		menu.setStorage(availableButtons);

		menu.open(user.getPlayer());
	}

	public static void showSimpleGui(Player player, Subject subject) {
		Anime.close(player);

		val menu = Selection.builder()
				.title(subject.getPrototype().getTitle())
				.hint("Открыть")
				.rows(3)
				.columns(3)
				.storage(
						getInformationFragment(subject),
						getColorChangeButton(player, subject),
						getRemoveSubjectButton(player, subject)
				).build();


		menu.open(player);
	}

	private static ReactiveButton getUpgradeButton(User user, SkeletonSubject subject) {
		val upgradeCost = 10000;
		val upgradePercent = 20;

		val button = new ReactiveButton()
				.texture("minecraft:mcpatcher/cit/others/hub/guild_invite.png")
				.title("Улучшить")
				.price(upgradeCost);

		if (subject.getLevel() >= 50) button.setHint("Максимальная\nпрокачка");
		else if (user.getMoney() < upgradeCost) button.setHint("Не хватает\nденег");
		else {
			button.description(
					"&b" + (subject.getLevel()) + " &fуровень ➠ &b" + (subject.getLevel() + 1) +
					"\n&a+" + subject.getLevel() * upgradePercent + "% ▲▲▲");
			button.hint("Купить");

			button.onClick((click, index, __) -> {
				user.giveMoney(-upgradeCost);
				subject.setLevel(subject.getLevel() + 1);
				Glow.animate(user.handle(), 0.4, GlowColor.GREEN);
				MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.fine("Вы улучшили витрину до §b" + subject.getLevel() + "§f уровня!"));
				BannerUtil.updateBanners(subject);
				user.updateIncome();
				showSketelonGui(user, subject);
			});
		}


		return button;
	}

	private static ReactiveButton getInformationFragment(Subject subject) {
		var description = "Доход: " + String.format("%.2f", subject.getIncome());
		if (subject instanceof SkeletonSubject) {
			val skeletonSubject = ((SkeletonSubject) subject);
			if (skeletonSubject.getSkeleton() != null && skeletonSubject.getSkeleton().getPrototype() != null)
				description += "\n§b" + skeletonSubject.getSkeleton().getPrototype().getTitle();
		}

		return new ReactiveButton()
				.texture("minecraft:mcpatcher/cit/others/stats.png")
				.title("Информация")
				.description(description)
				.hint("");
	}

	private static ReactiveButton getSkeletonsListButton(User user, SkeletonSubject subject) {
		return new ReactiveButton()
				.texture("minecraft:mcpatcher/cit/others/settings.png")
				.title("Скелеты")
				.onClick((click, index, button) -> getSkeletonListGui(user, subject));
	}

	private static ReactiveButton getColorChangeButton(Player player, Subject subject) {
		return new ReactiveButton()
				.texture("minecraft:mcpatcher/cit/others/clothes.png")
				.title("Изменить цвет")
				.description("Поменять цвет объекта")
				.onClick((click, index, button) -> showChangeColorGui(player, subject));
	}

	private static ReactiveButton getRemoveSubjectButton(Player player, Subject subject) {
		return new ReactiveButton()
				.texture("minecraft:mcpatcher/cit/others/badges/cancel.png")
				.title("Убрать")
				.hint("Убрать")
				.description("Убрать объект из музея")
				.onClick((click, index, button) -> {
					val allocation = subject.getAllocation();
					if (allocation == null) return;
					allocation.perform(PLAY_EFFECTS, HIDE_BLOCKS, HIDE_PIECES, DESTROY_DISPLAYABLE);
					BannerUtil.deleteBanners(subject);
					subject.setAllocation(null);
					subject.getOwner().updateIncome();

					player.getInventory().addItem(SubjectLogoUtil.encodeSubjectToItemStack(subject));
					Anime.close(player);
				});
	}

	private static void removeRelic(User user, RelicShowcaseSubject subject) {
		val subjectRelic = subject.getFragment();
		subject.setFragment(null);
		user.getInventory().addItem(subjectRelic.getItem());
		user.getRelics().put(subjectRelic.getUuid(), subjectRelic);
		subject.updateFragment();
		((Museum) user.getState()).updateIncrease();
		BannerUtil.updateBanners(subject);
		user.updateIncome();
		MessageUtil.find("relic-tacked").send(user);
		Anime.close(user.getPlayer());
	}
}
