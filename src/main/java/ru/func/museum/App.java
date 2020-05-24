package ru.func.museum;

import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.player.Archaeologist;

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
        archaeologistMap.put(player.getUniqueId(), MongoManager.load(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        archaeologistMap.remove(UUID.fromString(
                MongoManager.save(archaeologistMap.get(e.getPlayer().getUniqueId())).getUuid()
        ));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Archaeologist archaeologist = archaeologistMap.get(player.getUniqueId());
        ExcavationType lastExcavation = archaeologist.getLastExcavation();
        Excavation excavation = lastExcavation.getExcavation();
        // Игрок на раскопках, блок находится в шахте и блок над - воздух.
        if (archaeologist.isOnExcavation() &&
                !lastExcavation.equals(ExcavationType.NOOP) &&
                excavation.canBreak(block.getLocation()) &&
                block.getLocation().subtract(0, -1, 0).getBlock().getType().equals(Material.AIR)
        ) {
            for (Location location : archaeologist.getPickaxeType().getPickaxe().dig(player, block))
                if (excavation.canBreak(location) && location.subtract(0, -1, 0).getBlock().getType().equals(Material.AIR))
                    location.subtract(0, 1, 0).getBlock().setType(Material.AIR);
        } else
            e.setCancelled(true);
    }
}
