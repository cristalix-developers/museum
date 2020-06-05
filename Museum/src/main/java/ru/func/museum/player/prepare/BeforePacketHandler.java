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
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {

    private final BlockPosition dump = new BlockPosition(0, 0, 0);

    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        val connection = ((CraftPlayer) player).getHandle().playerConnection;
        val playerLocked = new AtomicBoolean(false);
        connection.networkManager.channel.pipeline().addBefore(
                "packet_handler",
                player.getName(),
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) {
                        try {
                            if (packet instanceof PacketPlayInUseEntity &&
                                    !playerLocked.get() &&
                                    ((PacketPlayInUseEntity) packet).a > 100_000_000
                            ) {
                                val useEntity = (PacketPlayInUseEntity) packet;
                                int clearId = useEntity.a - useEntity.a % 10;
                                int parentId = useEntity.a % 100_000_000 / 100_000;
                                int id = useEntity.a % 10_000 / 100;

                                val entity = App.getApp().getMuseumEntities()[parentId];
                                val subEntity = entity.getSubs()[id];
                                val clone = new AtomicBoolean(false);

                                archaeologist.getElementList().stream()
                                        .filter(element -> element.getParentId() == parentId && element.getId() == id)
                                        .findFirst()
                                        .ifPresent(element -> {
                                            clone.set(true);

                                            double cost = entity.getRare().getCost();
                                            double prize = cost + ((Pickaxe.RANDOM.nextFloat() - .5) * cost / 2);

                                            String value = String.format("%.2f", prize) + "$";
                                            player.sendMessage("" +
                                                    "§7[§l§bi§7] §7Вы нашли §l§6" +
                                                    subEntity.getTitle() +
                                                    ", §7этот фрагмент - дубликат, его цена §l§6+" +
                                                    value
                                            );
                                            player.sendTitle("§6Находка!", "§e+" + value);

                                            archaeologist.setMoney(archaeologist.getMoney() + prize);
                                        });

                                if (!clone.get()) {
                                    player.sendMessage("§7[§l§bi§7] §7Вы откопали новый фрагмент: §l" + subEntity.getTitle() + "§7!");
                                    player.sendTitle("§l§6Находка!", "§eобнаружен " + entity.getRare().getWord() + " фрагмент");

                                    archaeologist.getElementList().add(new Element(parentId, id, null));
                                }
                                int[] ids = new int[subEntity.getPieces().size()];
                                for (int i = 0; i < subEntity.getPieces().size(); i++)
                                    ids[i] = clearId + i;
                                val integer = new AtomicInteger(0);
                                MinecraftServer.getServer().postToMainThread(() -> {
                                    playerLocked.set(true);
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
                                                playerLocked.set(false);
                                                cancel();
                                            }
                                        }
                                    }.runTaskTimerAsynchronously(app, 5L, 3L);
                                });
                            } else if (packet instanceof PacketPlayInUseItem && archaeologist.isOnExcavation()) {
                                val pc = (PacketPlayInUseItem) packet;
                                if (pc.c.equals(EnumHand.OFF_HAND) || archaeologist.getLastExcavation()
                                        .getExcavation()
                                        .getExcavationGenerator()
                                        .fastCanBreak(pc.a.getX(), pc.a.getY(), pc.a.getZ())
                                )
                                    // Genius
                                    pc.a = dump;
                            } else if (packet instanceof PacketPlayInBlockDig) {
                                val bd = (PacketPlayInBlockDig) packet;
                                val lastExcavation = archaeologist.getLastExcavation();
                                val excavation = lastExcavation.getExcavation();

                                boolean valid = archaeologist.isOnExcavation() &&
                                        !lastExcavation.equals(ExcavationType.NOOP) &&
                                        excavation.getExcavationGenerator().fastCanBreak(bd.a.getX(), bd.a.getY(), bd.a.getZ());
                                if (valid && bd.c == PacketPlayInBlockDig.EnumPlayerDigType.STOP_DESTROY_BLOCK) {
                                    // Возвращение игрока в музей
                                    archaeologist.setBreakLess(archaeologist.getBreakLess() - 1);
                                    if (archaeologist.getBreakLess() == 0) {
                                        player.sendTitle("§6Раскопки завершены!", "до возвращения 10 сек.");
                                        player.sendMessage("§7[§l§bi§7] §7Раскопки подошли к концу, сейчас вернем вас в музей!");
                                        Bukkit.getScheduler().runTaskLater(app, () -> {
                                            archaeologist.setOnExcavation(false);
                                            PreparePlayer.INVENTORY.getPrepare().execute(player, archaeologist, app);
                                            archaeologist.getCurrentMuseum().load(app, archaeologist, player);
                                            archaeologist.setExcavationCount(archaeologist.getExcavationCount() + 1);
                                        }, 10 * 20L);
                                        return;
                                    }
                                    // Игрок на раскопках, блок находится в шахте
                                    archaeologist.giveExp(player, 5);
                                    int[] ids = excavation.getExcavationGenerator().getElementsId();
                                    int parentId = ids[Pickaxe.RANDOM.nextInt(ids.length)];

                                    val parent = App.getApp().getMuseumEntities()[parentId];

                                    int bingo = (int) Math.pow(10, parent.getRare().getRareScale());
                                    MinecraftServer.getServer().postToMainThread(() -> {
                                        archaeologist.getPickaxeType().getPickaxe().dig(connection, excavation, bd.a);
                                        if (Pickaxe.RANDOM.nextInt(bingo) + 1 == bingo) {
                                            archaeologist.giveExp(player, 25);
                                            SubEntity[] subEntity = parent.getSubs();
                                            int id = Pickaxe.RANDOM.nextInt(subEntity.length);
                                            subEntity[id].show(
                                                    connection,
                                                    new Location(Excavation.WORLD, bd.a.getX(), bd.a.getY(), bd.a.getZ()),
                                                    parentId,
                                                    id
                                            );
                                        }
                                    });
                                } else if (!valid && bd.c == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
                                    // Genius
                                    bd.c = PacketPlayInBlockDig.EnumPlayerDigType.ABORT_DESTROY_BLOCK;
                                }
                            }
                            super.channelRead(channelHandlerContext, packet);
                        } catch (Exception e) {
                            Bukkit.getConsoleSender().sendMessage("§cJarvis! We have some problems at BeforePacketHandler... " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}
