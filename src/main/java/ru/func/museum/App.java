package ru.func.museum;

import com.google.common.collect.Maps;
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
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.prepare.AfterDecoder;
import ru.func.museum.player.prepare.BeforePacketHandler;
import ru.func.museum.player.prepare.Prepare;

import java.util.*;

public final class App extends JavaPlugin implements Listener {

    private Map<UUID, Archaeologist> archaeologistMap = Maps.newHashMap();
    private List<Prepare> playerPrepares = new ArrayList<>(Arrays.asList(
            new AfterDecoder(),
            new BeforePacketHandler()
    ));

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
        Archaeologist archaeologist = MongoManager.load(player).getKey();
        archaeologistMap.put(player.getUniqueId(), archaeologist);
        playerPrepares.forEach(prepare -> prepare.execute(player, archaeologist, this));
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
