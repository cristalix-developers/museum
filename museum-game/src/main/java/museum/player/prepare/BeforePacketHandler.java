package museum.player.prepare;

import clepto.bukkit.B;
import clepto.bukkit.Cycle;
import clepto.bukkit.item.Items;
import implario.ListUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.val;
import me.func.mod.Anime;
import museum.App;
import museum.boosters.BoosterType;
import museum.excavation.Excavation;
import museum.fragment.Relic;
import museum.international.International;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.museum.Museum;
import museum.museum.subject.*;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.player.pickaxe.PickaxeType;
import museum.player.pickaxe.PickaxeUpgrade;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import ru.cristalix.core.formatting.Formatting;

import java.util.List;

import static museum.excavation.Excavation.isAir;
import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.*;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

	public static final Prepare INSTANCE = new BeforePacketHandler();

	public static final ItemStack EMERGENCY_STOP = Items.render("goback").asBukkitMirror();
	public static final V4 OFFSET = new V4(0, 0.03, 0, 4);
	public static final BlockPosition DUMMY = new BlockPosition(0, 0, 0);
	private static final ItemStack[] INTERACT_ITEMS = Items.items.keySet().stream()
			.filter(closure -> closure.contains("treasure"))
			.map(closure -> Items.render(closure).asBukkitMirror())
			.toArray(ItemStack[]::new);
	private static final ItemStack AIR_ITEM = ru.cristalix.core.item.Items.builder()
			.type(Material.AIR)
			.build();

	@Override
	public void execute(User user, App app) {
		user.getConnection().networkManager.channel.pipeline().addBefore("packet_handler", user.getName(), new ChannelDuplexHandler() {
			@Override
			public void channelRead(ChannelHandlerContext channelHandlerContext, Object packetObj) throws Exception {
				if (packetObj instanceof PacketPlayInUseItem) {
					MinecraftServer.SERVER.postToMainThread(() -> onItemUse(user, (PacketPlayInUseItem) packetObj));
				} else if (packetObj instanceof PacketPlayInBlockDig) {
					val dig = (PacketPlayInBlockDig) packetObj;
					// Если пакет о дропе предмета - дропнуть пакет
					if (dig.c == DROP_ITEM || dig.c == DROP_ALL_ITEMS)
						return;
					MinecraftServer.SERVER.postToMainThread(() -> onDigging(user, dig));
				} else if (packetObj instanceof PacketPlayInSteerVehicle && user.getRiding() != null) {
					// Если игрок на коллекторе и нажимает шифт, то скинуть его
					onUnmount(user, (PacketPlayInSteerVehicle) packetObj);
				}
				super.channelRead(channelHandlerContext, packetObj);
			}
		});
	}

	private void onUnmount(User user, PacketPlayInSteerVehicle packet) {
		if (packet.d) {
			if (user.getRiding() != null) {
				user.getRiding().passengers.clear();
				val dismount = new PacketPlayOutMount(user.getRiding());
				user.getState().getUsers().forEach(viewer -> viewer.sendPacket(dismount));
				user.setRiding(null);
			}
		}
	}

	private void onDigging(User user, PacketPlayInBlockDig packet) {
		val state = user.getState();
		if (state instanceof International) {
			((International) state).acceptBlockBreak(user, packet);
		} else if (packet.c == STOP_DESTROY_BLOCK && state instanceof Excavation && isAir(user, packet.a)) {
			if (tryReturnPlayer(user, false))
				return;
			acceptedBreak(user, packet);
		} else if (packet.c == START_DESTROY_BLOCK) {
			packet.c = ABORT_DESTROY_BLOCK;
			packet.a = DUMMY;
		}
	}

	private void onItemUse(User user, PacketPlayInUseItem packet) {
		if (packet.c == EnumHand.MAIN_HAND && isAir(user, packet.a) || isAir(user, packet.a.shift(packet.b))) {
			val itemInMainHand = user.getPlayer().getInventory().getItemInMainHand();

			if (user.getState() instanceof Museum)
				acceptMuseumClick(user, packet);
			else if (itemInMainHand != null && itemInMainHand.equals(EMERGENCY_STOP)) {
				if (user.getState() instanceof International) {
					B.postpone(10, () -> user.setState(user.getLastMuseum()));
				} else {
					tryReturnPlayer(user, true);
				}
			}
			packet.a = DUMMY;
		} else if (packet.c == EnumHand.OFF_HAND)
			packet.a = DUMMY;
	}

	private void acceptMuseumClick(User user, PacketPlayInUseItem packet) {
		Museum museum = (Museum) user.getState();
		BlockPosition pos = packet.a;
		if (packet.c == EnumHand.OFF_HAND)
			return;
		for (Subject subject : museum.getSubjects()) {
			for (Location loc : subject.getAllocation().getAllocatedBlocks()) {
				if (loc.getBlockX() == pos.getX() && loc.getBlockY() == pos.getY() && loc.getBlockZ() == pos.getZ()) {
					// Открытие меню настройки постройки
					packet.a = DUMMY; // Genius
					if (user != museum.getOwner())
						MessageUtil.find("non-root").send(user);
					else {
						subject.acceptClick();
						//Guis.open(user.getPlayer(), "manipulator", subject.getCachedInfo().getUuid());
						if (subject instanceof SkeletonSubject)
							SubjectGui.showSketelonGui(user, (SkeletonSubject) subject);
						else if (subject instanceof RelicShowcaseSubject)
							SubjectGui.showRelicGui(user, (RelicShowcaseSubject) subject);
						else SubjectGui.showSimpleGui(user.getPlayer(), subject);
					}
					break;
				}
			}
		}
		BeforePacketHandler.this.acceptSubjectPlace(user, museum, packet.a);
	}

	private void acceptSubjectPlace(User user, Museum museum, BlockPosition position) {
		if (museum == null || museum.getOwner() != user) return;

		val item = user.getInventory().getItemInMainHand();
		val subject = SubjectLogoUtil.decodeItemStackToSubject(user, item);

		if (subject == null)
			return;

		val location = new Location(App.getApp().getWorld(), position.getX(), position.getY(), position.getZ());

		if (subject.getPrototype().getAble() != location.getBlock().getType()) {
			MessageUtil.find("cannot-place").send(user);
			return;
		}

		Location origin = location.clone().add(0, 1, 0);
		if (!museum.addSubject(subject, origin)) {
			MessageUtil.find("cannot-place").send(user);
			return;
		}

		user.getInventory().setItemInMainHand(AIR_ITEM);

		subject.getAllocation().perform(Allocation.Action.UPDATE_BLOCKS, Allocation.Action.SPAWN_PIECES, Allocation.Action.SPAWN_DISPLAYABLE);

		for (User viewer : museum.getUsers())
			viewer.getPlayer().playSound(origin, Sound.BLOCK_STONE_PLACE, 1, 1);

		MessageUtil.find("placed").send(user);
		Anime.clearMarkers(user.handle());
	}

	private boolean tryReturnPlayer(User user, boolean force) {
		Excavation excavation = ((Excavation) user.getState());
		excavation.updateHits(user, excavation.getHitsLeft() - 1);

		if (excavation.getHitsLeft() < 0)
			return true;

		if (excavation.getHitsLeft() < 1 || force) {
			user.sendTitle("§7Раскопки завершены!\n\n\n§bВозвращаем вас...");
			MessageUtil.find("excavationend").send(user);
			B.postpone(100, () -> {
				user.setState(user.getLastMuseum());
				user.setExcavationCount(user.getExcavationCount() + 1);
			});
			excavation.updateHits(user, -1);
			return false;
		}
		return excavation.getHitsLeft() < 0;
	}

	@SuppressWarnings("deprecation")
	private void acceptedBreak(User user, PacketPlayInBlockDig packet) {
		if (user.getPlayer() == null || !(user.getState() instanceof Excavation))
			return;
		// Пишем игрокам с режимом отладки в том же чанке
		App.app.getUsers().stream()
				.filter(User::isDebug)
				.filter(it -> it.getPlayer().getChunk().equals(user.getPlayer().getChunk()))
				.forEach(it -> MultiChatUtil.sendMessage(it.getPlayer(), ChatType.SYSTEM, Formatting.fine(user.getPlayer().getName() + " сломал блок на " + packet.a.toString())));

		// С некоторым шансом может выпасть интерактивая вещь
		if (Vector.random.nextFloat() > .9)
			user.getPlayer().getInventory().addItem(ListUtils.random(INTERACT_ITEMS));
		// С некоторым шансом может выпасть реликвия
		if (Vector.random.nextFloat() > .998 - PickaxeUpgrade.DETECTION_OF_RELIQUES.convert(user)) {
			val relics = ((Excavation) user.getState()).getPrototype().getRelics();
			if (relics != null && relics.length > 0) {
				val randomRelic = new Relic(
						ListUtils.random(((Excavation) user.getState()).getPrototype().getRelics()).getAddress()
				);
				randomRelic.give(user);
				val item = randomRelic.getItem();
				val relicTitle = item.getItemMeta().getDisplayName();

				Anime.itemTitle(user.handle(), item, relicTitle, "Находка!", 1.4);
				MessageUtil.find("relic-find")
						.set("title", relicTitle)
						.send(user);
			}
		}
		Excavation excavation = (Excavation) user.getState();
		val excavationLvl = excavation.getPrototype().getRequiredLevel();

		if (LevelSystem.acceptGiveExp(user, excavationLvl)) {
			// Бонусы получения опыта
			double extra = Math.random() > 1 - PickaxeUpgrade.ADDITIONAL_EXP.convert(user) ? 1 : 0;
			// Если у игрока есть префикс сердечко - шанс получить один опыт
			if (user.getPrefix() != null && user.getPrefix().equals("䂋") && Math.random() < .10)
				extra += 1;

			user.giveExperience(PickaxeType.valueOf(user.getPickaxeType().name()).getExperience() + extra);
		}
		// Перебрать все кирки и эффекты на них
		for (PickaxeType pickaxeType : PickaxeType.values()) {
			if (pickaxeType.ordinal() <= user.getPickaxeType().ordinal()) {
				List<BlockPosition> positions = pickaxeType.getPickaxe().dig(user, packet.a);
				if (positions != null)
					positions.forEach(position -> generateFragments(user, position));
			}
		}
	}

	private void generateFragments(User user, BlockPosition position) {
		val prototype = ((Excavation) user.getState()).getPrototype();
		val proto = ListUtils.random(prototype.getAvailableSkeletonPrototypes());
		val playerChance = PickaxeUpgrade.BONE_DETECTION.convert(user) + 1;
		val bingo = proto.getRarity().getRareScale() * playerChance / 300D;
		val randomValue = Math.random();

		if (bingo > randomValue) {
			// Если повезло, то будет проиграна анимация и тд
			user.giveExperience(1);
			val fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));

			V4 location = new V4(position.getX(), position.getY() + 0.5, position.getZ(), (float) (Math.random() * 360 - 180));

			fragment.transpose(location).forEach((atom, origin) -> atom.show(user, origin));
			animateFragments(user, fragment, location);

			// Проверка на дубликат
			Skeleton skeleton = user.getSkeletons().supply(proto);

			if (skeleton.getUnlockedFragments().contains(fragment)) {
				double price = (proto.getPrice() + (proto.getPrice() * PickaxeUpgrade.DUPLICATE.convert(user)))
						* (7.5 + Math.random() * 5.0);
				Anime.cursorMessage(
						user.handle(),
						String.format("%s §6§l+%.2f$",
								fragment.getAddress(),
								price * App.getApp().getPlayerDataManager().calcMultiplier(user.getUuid(), BoosterType.COINS)
						)
				);
				user.depositMoneyWithBooster(price);
			} else {
				Anime.cursorMessage(user.handle(), "§lNEW! §b" + fragment.getAddress() + " §f㦶");
				skeleton.getUnlockedFragments().add(fragment);
				user.updateIncome();
			}
		}
	}

	private void animateFragments(User user, Fragment fragment, V4 location) {
		Cycle.run(1, 60, tick -> {
			if (tick == 59) fragment.transpose(OFFSET).keySet().forEach(piece -> piece.hide(user));
			else fragment.transpose(location.add(OFFSET)).forEach((atom, origin) -> atom.update(user, origin));
		});
	}

}
