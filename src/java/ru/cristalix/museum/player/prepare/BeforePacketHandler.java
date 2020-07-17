package ru.cristalix.museum.player.prepare;

import clepto.ListUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import ru.cristalix.museum.App;
import ru.cristalix.museum.excavation.Excavation;
import ru.cristalix.museum.excavation.ExcavationPrototype;
import ru.cristalix.museum.museum.subject.Subject;
import ru.cristalix.museum.museum.subject.skeleton.Fragment;
import ru.cristalix.museum.museum.subject.skeleton.Piece;
import ru.cristalix.museum.museum.subject.skeleton.Skeleton;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;
import ru.cristalix.museum.player.User;
import ru.cristalix.museum.player.pickaxe.Pickaxe;
import ru.cristalix.museum.player.pickaxe.PickaxeType;
import ru.cristalix.museum.util.MessageUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.*;
import static ru.cristalix.museum.excavation.Excavation.isAir;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

	private final BlockPosition dummy = new BlockPosition(0, 0, 0);

	@Override
	public void execute(User user, App app) {
		PlayerConnection connection = user.getConnection();
		connection.networkManager.channel.pipeline().addBefore("packet_handler", user.getName(),
				new ChannelDuplexHandler() {
					@Override
					public void channelRead(ChannelHandlerContext channelHandlerContext, Object packetObj) throws Exception {
						if (packetObj instanceof PacketPlayInUseItem) {
							PacketPlayInUseItem packet = (PacketPlayInUseItem) packetObj;
							if (packet.c == EnumHand.MAIN_HAND) {
								if (isAir(user, packet.a) || isAir(user, packet.a.shift(packet.b))) {
									if (user.getExcavation() == null) {
										for (Subject subject : user.getCurrentMuseum().getSubjects()) {
											if (subject.getAllocation() == null)
												continue;
											for (Location loc : subject.getAllocation().getAllocatedBlocks()) {
												BlockPosition pos = packet.a;
												if (loc.getBlockX() == pos.getX() && loc.getBlockY() == pos.getY() && loc.getBlockZ() == pos.getZ()) {
													packet.a = dummy; // Genius
													MinecraftServer.getServer().postToMainThread(() ->
															user.getCurrentMuseum().processClick(user, subject));
													break;
												}
											}
										}
									}
								}
							} else if (packet.c == EnumHand.OFF_HAND)
								packet.a = dummy;
						} else if (packetObj instanceof PacketPlayInBlockDig) {
							PacketPlayInBlockDig packet = (PacketPlayInBlockDig) packetObj;
							Excavation excavation = user.getExcavation();
							boolean valid = excavation != null && isAir(user, packet.a);

							if (packet.c == STOP_DESTROY_BLOCK && valid) {
								user.sendAnime();
								if (tryReturnPlayer(user, app)) return;
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

	private boolean tryReturnPlayer(User user, App app) {
		Excavation excavation = user.getExcavation();

		if (excavation.getHitsLeft() == -1)
			return true;

		excavation.setHitsLeft(excavation.getHitsLeft() - 1);
		if (excavation.getHitsLeft() == 0) {
			user.getPlayer().sendTitle("§6Раскопки завершены!", "до возвращения 10 сек.");
			MessageUtil.find("excavationend").send(user);
			excavation.setHitsLeft(-1);
			Bukkit.getScheduler().runTaskLater(app, () -> {
				user.setExcavation(null);
				PrepareSteps.INVENTORY.getPrepare().execute(user, app);
				user.getCurrentMuseum().show(user);
				user.setExcavationCount(user.getExcavationCount() + 1);
			}, 10 * 20L);
			return true;
		}
		return excavation.getHitsLeft() < 0;
	}

	@SuppressWarnings("deprecation")
	private void acceptedBreak(User user, PacketPlayInBlockDig packet) {
		MinecraftServer.getServer().postToMainThread(() -> {
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
		ExcavationPrototype prototype = user.getExcavation().getPrototype();
		SkeletonPrototype proto = ListUtils.random(prototype.getAvailableSkeletonPrototypes());

		double luckyBuffer = user.getLocation().getY() / 100;

		double bingo = luckyBuffer / proto.getRarity().getRareScale() / 10;
		if (bingo > Pickaxe.RANDOM.nextDouble()) {
			// Если повезло, то будет проиграна анимация и тд
			user.giveExperience(25);
			List<Fragment> fragments = proto.getFragments();
			Fragment fragment = ListUtils.random(fragments);

			fragment.show(user.getPlayer(), new Location(user.getWorld(), position.getX(), position.getY(), position.getZ()), true);

			animateFragments(user, fragment);

			// Проверка на дубликат
			Skeleton skeleton = user.getSkeletons().get(proto);

			if (skeleton.getUnlockedFragments().contains(fragment)) {
				double cost = proto.getRarity().getCost();
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

	private void animateFragments(User user, Fragment fragment) {
		AtomicInteger integer = new AtomicInteger(0);
		new BukkitRunnable() {
			@Override
			public void run() {
				PlayerConnection connection = user.getConnection();

				int[] ids = fragment.getPieces().stream()
						.map(Piece::getEntityId)
						.mapToInt(Integer::valueOf)
						.toArray();

				if (integer.getAndIncrement() < 22) {
					for (int id : ids) {
						connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(id, 0, 3, 0, false));
						byte angle = (byte) (5 * integer.get() % 128);
						connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(id, angle, (byte) 0, false));
					}
				} else {
					connection.sendPacket(new PacketPlayOutEntityDestroy(ids));
					cancel();
				}
			}
		}.runTaskTimerAsynchronously(App.getApp(), 5L, 3L);
	}
}
