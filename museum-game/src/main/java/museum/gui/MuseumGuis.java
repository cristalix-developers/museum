package museum.gui;

import clepto.bukkit.Lemonade;
import clepto.bukkit.gui.Guis;
import clepto.humanize.TimeFormatter;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.excavation.ExcavationPrototype;
import museum.museum.Museum;
import museum.museum.map.SkeletonSubjectPrototype;
import museum.museum.map.SubjectType;
import museum.museum.subject.SkeletonSubject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.item.Items;

import java.time.Duration;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@UtilityClass
public class MuseumGuis {

	public final ItemStack AIR_ITEM = new ItemStack(Material.AIR);

	public void registerItemizers(App app) {
		ItemStack lockItem = Lemonade.get("lock").render();

		Guis.registerItemizer("subjects-select-dino", (base, player, context, slotId) -> {
			val user = app.getUser(player);
			SkeletonPrototype skeletonType;
			SkeletonSubject subject;

			int index = context.getOpenedGui().getIndex(slotId);

			try {
				skeletonType = requireNonNull(Managers.skeleton.getByIndex(index));
				subject = requireNonNull((SkeletonSubject) user.getCurrentMuseum().getSubjectByUuid(UUID.fromString(context.getPayload())));
				requireNonNull(user.getSkeletons().supply(skeletonType));
			} catch (Exception e) {
				return lockItem;
			}

			if (skeletonType.getSize() > ((SkeletonSubjectPrototype) subject.getPrototype()).getSize())
				return Lemonade.get("too-huge").dynamic().fill("dino", skeletonType.getTitle()).render();

			// Если любая витрина уже использует этот прототип, то поставить lock предмет
			for (SkeletonSubject skeletonSubject : user.getCurrentMuseum().getSubjects(SubjectType.SKELETON_CASE)) {
				Skeleton skeleton = skeletonSubject.getSkeleton();
				if (skeleton == null) continue;
				if (skeleton.getCachedInfo().getPrototypeAddress().equals(skeletonType.getAddress()))
					return lockItem;
			}

			Skeleton playerSkeleton = null;

			for (Skeleton skeleton : user.getSkeletons())
				if (skeleton.getPrototype().getAddress().equals(skeletonType.getAddress()))
					playerSkeleton = skeleton;

			if (playerSkeleton == null)
				return lockItem;

			return Items.builder()
					.displayName(skeletonType.getTitle() + " / " + skeletonType.getRarity().getWord().toUpperCase())
					.type(Material.BONE_BLOCK)
					.lore("Собрано " + playerSkeleton.getUnlockedFragments().size() + "/" + skeletonType.getFragments().size())
					.build();
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
