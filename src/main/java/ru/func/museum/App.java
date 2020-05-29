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
import ru.func.museum.element.Element;
import ru.func.museum.element.ElementType;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.player.Archaeologist;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        connection.networkManager.channel.pipeline().addBefore(
                "packet_handler",
                player.getName(),
                new ChannelDuplexHandler() {
                    @Override
                    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                        // Bukkit.getServer().getConsoleSender().sendMessage("PACKET READ: " + packet.toString());
                        if (packet instanceof PacketPlayInUseEntity) {
                            int entityId = ((PacketPlayInUseEntity) packet).a;
                            ElementType type = ElementType.findTypeById(entityId);
                            if (type != null) {
                                for (Element element : archaeologist.getElementList()) {
                                    if (element.getType().equals(type)) {
                                        element.setCount(element.getCount() + 1);
                                        break;
                                    }
                                }
                                player.sendMessage("Вы получили " + type.getTitle() + ", его редкость: " + type.getElementRare().getName());
                                int[] ids = new int[type.getPieces()];
                                for (int i = 0; i < ids.length; i++)
                                    ids[i] = new Integer(("" + entityId).substring(0, 5) + (10 + i) + "" + type.getId());
                                MinecraftServer.getServer().postToMainThread(() ->
                                        connection.sendPacket(new PacketPlayOutEntityDestroy(ids)));
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
