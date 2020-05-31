package ru.func.museumparser;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.func.museumparser.command.SerializeEntity;

public final class MuseumParser extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("serialize").setExecutor(new SerializeEntity(this));
        Bukkit.getConsoleSender().sendMessage("§aMuseum serializer successfully activated. Using GSON.");
    }
}
