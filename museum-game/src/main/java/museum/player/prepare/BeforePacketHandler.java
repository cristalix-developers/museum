package museum.player.prepare;

import clepto.ListUtils;
import clepto.bukkit.B;
import clepto.bukkit.Lemonade;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.val;
import museum.App;
import museum.excavation.Excavation;
import museum.excavation.ExcavationPrototype;
import museum.museum.subject.CollectorSubject;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.*;
import museum.player.User;
import museum.player.pickaxe.Pickaxe;
import museum.player.pickaxe.PickaxeType;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static museum.excavation.Excavation.isAir;
import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.*;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

	private final BlockPosition dummy = new BlockPosition(0, 0, 0);
	private final ItemStack menu = Lemonade.get("menu").render();

	@Override
	public void execute(User user, App app) {
		PlayerConnection connection = user.getConnection();
		connection.networkManager.channel.pipeline().addBefore("packet_handler", user.getName(),
				new ChannelDuplexHandler() {
					@Override
					public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
						try {
							// Обработка отправки чанков
							if (msg instanceof PacketPlayOutMapChunk) {
								val mapChunk = (PacketPlayOutMapChunk) msg;
								if (user.getExcavation() != null) {
									// Если загружается чанк шахты, то новой
									val prototype = user.getExcavation().getPrototype();
									for (PacketPlayOutMapChunk packet : prototype.getPackets()) {
										if (packet.a == mapChunk.a && packet.b == mapChunk.b) {
											super.write(ctx, packet, promise);
											return;
										}
									}
								} else {
									for (Subject subject : user.getCurrentMuseum().getSubjects()) {
										val allocation = subject.getAllocation();
										if (allocation == null) continue;
										B.postpone(10, () -> {
											allocation.getShowPackets().forEach(packet -> {
												if (packet.a.getX() / 16 == mapChunk.a && packet.a.getZ() / 16 == mapChunk.b)
													user.sendPacket(packet);
											});
											if (subject instanceof CollectorSubject) {
												val loc = ((CollectorSubject) subject).getCollectorLocation();
												if (loc.getBlockX() / 16 == mapChunk.a && loc.getBlockZ() / 16 == mapChunk.b) {
													val piece = ((CollectorSubject) subject).getPiece();
													piece.hide(user.getPlayer());
													piece.show(user.getPlayer(), loc);
												}
											}
										});
									}
								}
							}
						} catch (Exception ignored) {}
						if (msg != null) {
							super.write(ctx, msg, promise);
						}
					}

					@SuppressWarnings ("deprecation")
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
									packet.a = dummy;
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
				val inventory = user.getPlayer().getInventory();
				inventory.clear();
				inventory.setItem(0, menu);
				user.getCurrentMuseum().show(user);
				user.setExcavationCount(user.getExcavationCount() + 1);
			}, 10 * 20L);
			return true;
		}
		return excavation.getHitsLeft() < 0;
	}

	@SuppressWarnings ("deprecation")
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

//		double bingo = luckyBuffer / proto.getRarity().getRareScale() / 10;
		double bingo = 1;
		if (bingo > Pickaxe.RANDOM.nextDouble()) {
			// Если повезло, то будет проиграна анимация и тд
			user.giveExperience(25);
			Fragment fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));

			V4 location = new V4(position.getX(), position.getY(), position.getZ(), (float) (Math.random() * 360 - 180));
			location.add(0, 0.5, 0);
			fragment.show(user.getPlayer(), location);

			animateFragments(user, fragment, location);

			// Проверка на дубликат
			Skeleton skeleton = user.getSkeletons().get(proto);

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
		AtomicInteger integer = new AtomicInteger(0);
		new BukkitRunnable() {
			@Override
			public void run() {
				PlayerConnection connection = user.getConnection();

				int[] ids = fragment.getPieceOffsetMap().keySet().stream()
						.mapToInt(piece -> piece.getStand().id)
						.toArray();

				if (integer.getAndIncrement() < 22) {
					for (int id : ids) {
						connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(id, 0, 3, 0, false));
						byte angle = (byte) (5 * integer.get() % 128);
						// ToDo: Разве оно не сломает структуру фрагмента из нескольких стендов?
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
