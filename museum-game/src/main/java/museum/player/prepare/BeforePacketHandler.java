package museum.player.prepare;

import clepto.ListUtils;
import clepto.bukkit.B;
import clepto.bukkit.Cycle;
import clepto.bukkit.Lemonade;
import clepto.bukkit.item.Items;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.val;
import museum.App;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.gui.MuseumGuis;
import museum.museum.Museum;
import museum.museum.subject.Allocation;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.player.pickaxe.PickaxeType;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

import static museum.excavation.Excavation.isAir;
import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.*;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

	public static final BeforePacketHandler INSTANCE = new BeforePacketHandler();

	public static final ItemStack EMERGENCY_STOP = Items.render("go-back-item").asBukkitMirror();
	public static final V4 OFFSET = new V4(0, 0.03, 0, 4);
	private static final ItemStack emeraldItem = Items.render("emerald-item").asBukkitCopy();
	private static final BlockPosition dummy = new BlockPosition(0, 0, 0);

	@Override
	public void execute(User user, App app) {
		user.getConnection().networkManager.channel.pipeline().addBefore("packet_handler", user.getName(),
				new ChannelDuplexHandler() {
					@SuppressWarnings ("deprecation")
					@Override
					public void channelRead(ChannelHandlerContext channelHandlerContext, Object packetObj) throws Exception {
						if (packetObj instanceof PacketPlayInUseItem) {
							PacketPlayInUseItem packet = (PacketPlayInUseItem) packetObj;
							if (packet.c == EnumHand.MAIN_HAND) {
								if (isAir(user, packet.a) || isAir(user, packet.a.shift(packet.b))) {
									val itemInMainHand = user.getPlayer().getInventory().getItemInMainHand();

									if (user.getState() instanceof Museum) {
										Museum museum = (Museum) user.getState();
										for (Subject subject : museum.getSubjects()) {
											for (Location loc : subject.getAllocation().getAllocatedBlocks()) {
												BlockPosition pos = packet.a;
												if (loc.getBlockX() == pos.getX() && loc.getBlockY() == pos.getY() && loc.getBlockZ() == pos.getZ()) {
													packet.a = dummy; // Genius
													MinecraftServer.getServer().postToMainThread(() -> {
														if (user != museum.getOwner()) MessageUtil.find("non-root").send(user);
														else user.performCommand("gui manipulator " + subject.getCachedInfo().getUuid());
													});
													break;
												}
											}
										}
										BlockPosition blockPos = new BlockPosition(packet.a);
										B.run(() -> BeforePacketHandler.this.acceptSubjectPlace(user, museum, blockPos));
									} else if (itemInMainHand != null && itemInMainHand.equals(EMERGENCY_STOP))
										tryReturnPlayer(user, true);
									packet.a = dummy;
								}
							} else if (packet.c == EnumHand.OFF_HAND)
								packet.a = dummy;
						} else if (packetObj instanceof PacketPlayInBlockDig) {
							PacketPlayInBlockDig packet = (PacketPlayInBlockDig) packetObj;
							boolean valid = user.getState() instanceof Excavation && isAir(user, packet.a);
							if (packet.c == STOP_DESTROY_BLOCK && valid) {
								user.sendAnime();
								if (tryReturnPlayer(user, false))
									return;
								acceptedBreak(user, packet);
							} else if (packet.c == START_DESTROY_BLOCK) {
								packet.c = ABORT_DESTROY_BLOCK;
								packet.a = dummy;
							}
						}
						super.channelRead(channelHandlerContext, packetObj);
					}
				}
																		);
	}

	private void acceptSubjectPlace(User user, Museum museum, BlockPosition a) {
		if (museum == null || museum.getOwner() != user) return;

		val item = user.getInventory().getItemInMainHand();
		val subject = SubjectLogoUtil.decodeItemStackToSubject(user, item);

		if (subject == null)
			return;

		val location = new Location(App.getApp().getWorld(), a.getX(), a.getY(), a.getZ());

		if (subject.getPrototype().getAble() != location.getBlock().getType()) {
			MessageUtil.find("cannot-place").send(user);
			return;
		}

		Location origin = new Location(user.getWorld(), a.getX(), a.getY() + 1, a.getZ());
		Collection<User> viewers = museum.getUsers();
		if (!museum.addSubject(subject, origin)) {
			MessageUtil.find("cannot-place").send(user);
			return;
		}

		user.getInventory().setItemInMainHand(MuseumGuis.AIR_ITEM);
		subject.getAllocation().perform(Allocation.Action.UPDATE_BLOCKS, Allocation.Action.SPAWN_PIECES);
		for (User viewer : viewers) {
			viewer.getPlayer().playSound(origin, Sound.BLOCK_STONE_PLACE, 1, 1);
		}

		MessageUtil.find("placed").send(user);
	}

	private boolean tryReturnPlayer(User user, boolean force) {
		Excavation excavation = ((Excavation) user.getState());

		if (excavation.getHitsLeft() == -1)
			return true;

		excavation.setHitsLeft(excavation.getHitsLeft() - 1);
		if (excavation.getHitsLeft() == 0 || force) {
			user.getPlayer().sendTitle("§6Раскопки завершены!", "до возвращения 10 сек.");
			MessageUtil.find("excavationend").send(user);
			excavation.setHitsLeft(-1);
			B.postpone(200, () -> {
				user.setState(user.getLastMuseum());
				user.setExcavationCount(user.getExcavationCount() + 1);
			});
			return true;
		}
		return excavation.getHitsLeft() < 0;
	}

	@SuppressWarnings ("deprecation")
	private void acceptedBreak(User user, PacketPlayInBlockDig packet) {
		MinecraftServer.getServer().postToMainThread(() -> {
			// С некоторым шансом может выпасть эмеральд
			if (Vector.random.nextFloat() > .9)
				user.getPlayer().getInventory().addItem(emeraldItem);
			// Перебрать все кирки и эффекты на них
			for (PickaxeType pickaxeType : PickaxeType.values()) {
				if (pickaxeType.ordinal() <= user.getPickaxeType().ordinal()) {
					List<BlockPosition> positions = pickaxeType.getPickaxe().dig(user, packet.a);
					user.giveExperience(pickaxeType.getExperience());
					if (positions != null)
						positions.forEach(position -> generateFragments(user, position));
				}
			}
		});
	}

	private void generateFragments(User user, BlockPosition position) {
		ExcavationPrototype prototype = ((Excavation) user.getState()).getPrototype();
		SkeletonPrototype proto = ListUtils.random(prototype.getAvailableSkeletonPrototypes());

		val luckyBuffer = user.getLocation().getY() / 100;
		val bingo = luckyBuffer / proto.getRarity().getRareScale() / 10;

		if (bingo > Vector.random.nextDouble()) {
			// Если повезло, то будет проиграна анимация и тд
			user.giveExperience(1);
			val fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));

			V4 location = new V4(position.getX(), position.getY() + 0.5, position.getZ(), (float) (Math.random() * 360 - 180));

			fragment.transpose(location).forEach((atom, origin) -> atom.show(user, origin));
			animateFragments(user, fragment, location);

			// Проверка на дубликат
			Skeleton skeleton = user.getSkeletons().supply(proto);

			if (skeleton.getUnlockedFragments().contains(fragment)) {
				double cost = proto.getPrice();
				double prize = cost * (.75 + Math.random() * .50);

				String value = String.format("%.2f$", prize);

				MessageUtil.find("findcopy")
						.set("name", fragment.getAddress())
						.set("cost", value)
						.send(user);

				user.getPlayer().sendTitle("§6Находка!", "§e+" + value);

				user.setMoney(user.getMoney() + prize);

			} else {
				MessageUtil.find("findfragment")
						.set("name", fragment.getAddress())
						.send(user);
				user.getPlayer().sendTitle("§l§6Находка!", "§eобнаружен " + proto.getRarity().getWord() + " фрагмент");

				skeleton.getUnlockedFragments().add(fragment);
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
