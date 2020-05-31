package ru.func.museum.player.prepare;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.func.museum.App;
import ru.func.museum.element.ElementType;
import ru.func.museum.player.Archaeologist;

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
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                        if (packet instanceof PacketPlayInUseEntity && !playerLocked.get()) {
                            PacketPlayInUseEntity useEntity = (PacketPlayInUseEntity) packet;
                            // 1234_01_xxx -> 1234_00_xxx
                            System.out.println(useEntity.a);
                            int entityId = useEntity.a / 100000 * 100000 + 10000 + useEntity.a % 1000;
                            System.out.println(entityId);
                            ElementType type = ElementType.findTypeById(entityId % 1000);
                            if (type != null) {
                                archaeologist.getElementList().stream()
                                        .filter(element -> element.getType().equals(type))
                                        .findFirst()
                                        .ifPresent(element -> element.setCount(element.getCount() + 1));
                                player.sendMessage("§6Вы нашли " + type.getTitle() + ", его редкость: " + type.getElementRare().getName());
                                player.sendTitle("§l§6Находка!", "§eобнаружен " + type.getElementRare().getWord() + " фрагмент");
                                int[] ids = new int[type.getPieces()];
                                for (int i = 0; i < type.getPieces(); i++)
                                    ids[i] = entityId + i * 1000;
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
                        }
                        super.channelRead(channelHandlerContext, packet);
                    }
                }
        );
    }
}
