package ru.func.museum.player.prepare;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.element.deserialized.MuseumEntity;
import ru.func.museum.element.deserialized.SubEntity;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class BeforePacketHandler implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        final AtomicBoolean playerLocked = new AtomicBoolean(false);
        connection.networkManager.channel.pipeline().addBefore(
                "packet_handler",
                player.getName(),
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) {
                        try {
                            if (packet instanceof PacketPlayInUseEntity && !playerLocked.get()) {
                                PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) packet;
                                System.out.println(useEntity.a);
                                int clearId = useEntity.a - useEntity.a % 10;
                                int parentId = useEntity.a % 100_000_000 / 100_000;

                                System.out.println("clear: " + clearId);
                                System.out.println("parentId: " + parentId);

                                MuseumEntity entity = App.getApp().getMuseumEntities()[parentId];
                                System.out.println("len: " + entity.getSubs().length);
                                System.out.println(useEntity.a % 1000);
                                SubEntity subEntity = entity.getSubs()[useEntity.a % 10_000 / 100];

                                AtomicBoolean clone = new AtomicBoolean(false);

                                archaeologist.getElementList().stream()
                                        .filter(element -> element.getPiece().equals(subEntity))
                                        .findFirst()
                                        .ifPresent(element -> {
                                            clone.set(true);

                                            double cost = entity.getRare().getCost();
                                            double prize = cost + ((Pickaxe.RANDOM.nextFloat() - .5) * cost / 2);

                                            String value = String.format("%.2f", prize) + "$";
                                            player.sendMessage("" +
                                                    "§6Мастер, вы нашли " +
                                                    subEntity.getTitle() +
                                                    ", это вы уже находили, продам его за " +
                                                    value
                                            );
                                            player.sendTitle("§l§6Находка!", "§e+" + value);

                                            archaeologist.setMoney(archaeologist.getMoney() + prize);
                                        });

                                if (!clone.get()) {
                                    player.sendMessage("§6Вы нашли " + subEntity.getTitle() + ", его редкость: " + entity.getRare().getName());
                                    player.sendTitle("§l§6Находка!", "§eобнаружен " + entity.getRare().getWord() + " фрагмент");

                                    archaeologist.getElementList().add(new Element(subEntity, null));
                                }
                                int[] ids = new int[subEntity.getPieces().size()];
                                for (int i = 0; i < subEntity.getPieces().size(); i++)
                                    ids[i] = clearId + i;
                                AtomicInteger integer = new AtomicInteger(0);
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
