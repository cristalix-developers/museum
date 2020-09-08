package museum.player.prepare;

import clepto.ListUtils;
import clepto.bukkit.B;
import clepto.bukkit.Cycle;
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
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.player.pickaxe.Pickaxe;
import museum.player.pickaxe.PickaxeType;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;

import java.util.List;

import static museum.excavation.Excavation.isAir;
import static net.minecraft.server.v1_12_R1.PacketPlayInBlockDig.EnumPlayerDigType.*;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

	public static final BeforePacketHandler INSTANCE = new BeforePacketHandler();
	private static final BlockPosition dummy = new BlockPosition(0, 0, 0);

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
												if (packet.a.x == mapChunk.a && packet.a.z / 16 == mapChunk.b)
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
						} catch (Exception ignored) {
						}
						if (msg != null) {
							super.write(ctx, msg, promise);
						}
					}

					@SuppressWarnings("deprecation")
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
			B.postpone(200, () -> {
				user.setExcavation(null);
				user.getCurrentMuseum().show(user);
				user.setExcavationCount(user.getExcavationCount() + 1);
			});
			return true;
		}
		return excavation.getHitsLeft() < 0;
	}

	@SuppressWarnings("deprecation")
	private void acceptedBreak(User user, PacketPlayInBlockDig packet) {
		MinecraftServer.getServer().postToMainThread(() -> {
			// С некоторым шансом может выпасть эмеральд
			if (Pickaxe.RANDOM.nextFloat() > .9)
				user.getPlayer().getInventory().addItem(Lemonade.get("emerald-item").render());
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
		ExcavationPrototype prototype = user.getExcavation().getPrototype();
		SkeletonPrototype proto = ListUtils.random(prototype.getAvailableSkeletonPrototypes());

		double luckyBuffer = user.getLocation().getY() / 100;

//		double bingo = luckyBuffer / proto.getRarity().getRareScale() / 10;
		double bingo = 1;
		if (bingo > Pickaxe.RANDOM.nextDouble()) {
			// Если повезло, то будет проиграна анимация и тд
			user.giveExperience(1);
			Fragment fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));

			V4 location = new V4(position.getX(), position.getY() + 0.5, position.getZ(), (float) (Math.random() * 360 - 180));

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

	public static final V4 offset = new V4(0, 0.03, 0, 4);

	private void animateFragments(User user, Fragment fragment, V4 location) {
		Cycle.run(1, 60, tick -> {
			if (tick == 59) fragment.hide(user.getPlayer());
			else fragment.update(user.getPlayer(), location.add(offset));
		});
	}

}
