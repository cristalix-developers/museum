package ru.func.museum.player.prepare;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.val;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.element.deserialized.SubEntity;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.player.User;
import ru.func.museum.player.pickaxe.Pickaxe;
import ru.func.museum.player.pickaxe.PickaxeType;
import ru.func.museum.util.MessageUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

    private final BlockPosition dump = new BlockPosition(0, 0, 0);

    @Override
    public void execute(User user, App app) {
        val connection = user.getConnection();
        connection.networkManager.channel.pipeline().addBefore(
                "packet_handler",
                user.getName(),
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                        if (packet instanceof PacketPlayInUseItem && user.isOnExcavation()) {
                            val pc = (PacketPlayInUseItem) packet;
                            if (pc.c.equals(EnumHand.OFF_HAND) || user.getLastExcavation()
                                    .getExcavation()
                                    .getExcavationGenerator()
                                    .fastCanBreak(pc.a.getX(), pc.a.getY(), pc.a.getZ())
                            )
                                pc.a = dump; // Genius
                        } else if (packet instanceof PacketPlayInBlockDig) {
                            val bd = (PacketPlayInBlockDig) packet;
                            val lastExcavation = user.getLastExcavation();
                            val excavation = lastExcavation.getExcavation();

                            boolean valid = user.isOnExcavation() &&
                                    !lastExcavation.equals(ExcavationType.NOOP) &&
                                    excavation.getExcavationGenerator().fastCanBreak(bd.a.getX(), bd.a.getY(), bd.a.getZ());

                            if (valid && bd.c == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                                // Обновляю текст с кол-вом оставшихся ударов
                                user.sendAnime();
                                // Возвращение игрока в музей
                                if (tryReturnPlayer(user, app))
                                    return;
                                // Игрок на раскопках, блок находится в шахте
                                acceptedBreak(user, excavation, bd, app);
                            } else if (!valid && bd.c == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK)
                                bd.c = PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK; // Genius
                        }
                        super.channelRead(channelHandlerContext, packet);
                    }
                }
        );
    }

    private boolean tryReturnPlayer(User user, App app) {
        if (user.getBreakLess() == -1)
            return true;

        user.setBreakLess(user.getBreakLess() - 1);
        if (user.getBreakLess() == 0) {
            user.sendTitle("§6Раскопки завершены!", "до возвращения 10 сек.");
            MessageUtil.find("excavationend").send(user);
            user.setBreakLess(-1);
            Bukkit.getScheduler().runTaskLater(app, () -> {
                user.setOnExcavation(false);
                PrepareSteps.INVENTORY.getPrepare().execute(user, app);
                user.getCurrentMuseum().load(app, user);
                user.setExcavationCount(user.getExcavationCount() + 1);
            }, 10 * 20L);
            return true;
        }
        return user.getBreakLess() < 0;
    }

    private void acceptedBreak(User archaeologist, Excavation excavation, PacketPlayInBlockDig bd, App app) {
        archaeologist.giveExperience(5);

        MinecraftServer.getServer().postToMainThread(() -> {
            for (PickaxeType pickaxeType : PickaxeType.values()) {
                if (pickaxeType.getPrice() <= archaeologist.getPickaxeType().getPrice()) {
                    List<BlockPosition> positions = pickaxeType.getPickaxe().dig(archaeologist.getConnection(), excavation, bd.a);
                    if (positions != null)
                        positions.forEach(position -> generateFragments(archaeologist, excavation, position, app));
                }
            }
        });
    }

    private void generateFragments(User user, Excavation excavation, BlockPosition position, App app) {
        int[] ableIds = excavation.getExcavationGenerator().getElementsId();
        int parentId = ableIds[Pickaxe.RANDOM.nextInt(ableIds.length)];

        val parent = app.getMuseumEntities()[parentId];
        val generator = excavation.getExcavationGenerator();

        double luckyBuffer = (double) (generator.getCenter().getBlockY() - user.getLocation().getBlockY()) / generator.getDepth(); // no zero

        double bingo = luckyBuffer / parent.getRare().getRareScale() / 10;
        if (bingo > Pickaxe.RANDOM.nextDouble()) {
            // Если повезло, то будет проиграна анимация и тд
            user.giveExperience(25);
            SubEntity[] subEntities = parent.getSubs();

            val id = Pickaxe.RANDOM.nextInt(subEntities.length);
            val subEntity = subEntities[id];

            int[] ids = new int[subEntity.getPieces().size()];

            int noise = Pickaxe.RANDOM.nextInt(100_000);

            for (int i = 0; i < subEntity.getPieces().size(); i++)
                ids[i] = noise * 100 + i;

            subEntity.show(
                    user.getConnection(),
                    new Location(Excavation.WORLD, position.getX(), position.getY(), position.getZ()),
                    0,
                    noise,
                    true
            );

            animateFragments(user.getConnection(), ids, app);

            // Проверка на дубликат
			Optional<Element> elementOptional = user.getElementList().stream()
					.filter(element -> element.getParentId() == parentId && element.getId() == id)
					.findFirst();

			if (elementOptional.isPresent()) {
				double cost = parent.getRare().getCost();
				double prize = cost + ((Pickaxe.RANDOM.nextFloat() - .5) * cost / 2);

				String value = String.format("%.2f$", prize);

				MessageUtil.find("findcopy")
						.set("name", subEntity.getTitle())
						.set("cost", value)
						.send(user);

				user.sendTitle("§6Находка!", "§e+" + value);

				user.setMoney(user.getMoney() + prize);

			} else {
                MessageUtil.find("findfragment")
                        .set("name", subEntity.getTitle())
                        .send(user);
                user.sendTitle("§l§6Находка!", "§eобнаружен " + parent.getRare().getWord() + " фрагмент");

                user.getElementList().add(new Element(parentId, id, false, parent.getRare().getIncrease()));
            }
        }
    }

    private void animateFragments(PlayerConnection connection, int[] ids, App app) {
        val integer = new AtomicInteger(0);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (integer.getAndIncrement() < 22) {
                    for (int id : ids) {
                        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutRelEntityMove(
                                id, 0, 408, 0, false
                        ));
                        byte angle = (byte) (5 * integer.get() % 128);
                        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(
                                id, angle, (byte) 0, false
                        ));
                    }
                } else {
                    connection.sendPacket(new PacketPlayOutEntityDestroy(ids));
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(app, 5L, 3L);
    }
}
