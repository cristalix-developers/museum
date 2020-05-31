package ru.func.museum;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.func.museum.element.ElementType;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.player.Archaeologist;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class App extends JavaPlugin implements Listener {

    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        MongoManager.connect(
                getConfig().getString("uri"),
                getConfig().getString("database"),
                getConfig().getString("collection")
        );
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Archaeologist archaeologist = MongoManager.load(player);
        archaeologistMap.put(player.getUniqueId(), archaeologist);
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        App plugin = this;
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
                                                    byte angle = (byte) (5 * integer.get() % 128) ;
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
                                    }.runTaskTimerAsynchronously(plugin, 5L, 3L);
                                });
                            }
                        }
                        super.channelRead(channelHandlerContext, packet);
                    }
                }
        );
        connection.networkManager.channel.pipeline().addAfter("decoder",
                UUID.randomUUID().toString(), new MessageToMessageDecoder<Packet>() {
                    @Override
                    protected void decode(ChannelHandlerContext channelHandlerContext, Packet packet, List<Object> list) {
                        if (archaeologist.isOnExcavation() && packet instanceof PacketPlayInUseItem) {
                            PacketPlayInUseItem pc = (PacketPlayInUseItem) packet;
                            if (pc.c.equals(EnumHand.OFF_HAND) || archaeologist.getLastExcavation()
                                    .getExcavation()
                                    .getExcavationGenerator()
                                    .fastCanBreak(pc.a.getX(), pc.a.getY(), pc.a.getZ())
                            ) {
                                return;
                            }
                        }
                        list.add(packet);
                    }
                }
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        archaeologistMap.remove(UUID.fromString(
                MongoManager.save(archaeologistMap.get(e.getPlayer().getUniqueId())).getUuid()
        ));
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        archaeologistMap.get(e.getPlayer().getUniqueId()).getLastExcavation().getExcavation().getExcavationGenerator().generateAndShow(e.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Archaeologist archaeologist = archaeologistMap.get(player.getUniqueId());
        ExcavationType lastExcavation = archaeologist.getLastExcavation();
        Excavation excavation = lastExcavation.getExcavation();
        Location blockLocation = block.getLocation();
        // Игрок на раскопках, блок находится в шахте и блок над - воздух.
        if (archaeologist.isOnExcavation() &&
                !lastExcavation.equals(ExcavationType.NOOP) &&
                excavation.getExcavationGenerator().fastCanBreak(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ())
        ) {
            archaeologist.getPickaxeType().getPickaxe().dig(((CraftPlayer) player).getHandle().playerConnection, excavation, block);
        } else
            e.setCancelled(true);
    }
}
