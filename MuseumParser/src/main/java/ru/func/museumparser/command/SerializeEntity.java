package ru.func.museumparser.command;

import com.google.gson.Gson;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.func.museumparser.MuseumParser;
import ru.func.museumparser.entity.ElementRare;
import ru.func.museumparser.entity.MuseumEntity;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author func 31.05.2020
 * @project MuseumParser
 */
@RequiredArgsConstructor
public class SerializeEntity implements CommandExecutor {

    private Gson gson = new Gson();
    @NonNull
    private MuseumParser plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("Serializer opened only for admin.");
            return true;
        }

        String title = Stream.of(args).skip(3).collect(Collectors.joining(" "));

        try {
            plugin.getConfig().set("data", gson.toJson(new MuseumEntity(
                    player.getLocation(),
                    Integer.parseInt(args[0]),
                    Integer.parseInt(args[1]),
                    ElementRare.valueOf(args[2]),
                    title
            )));
            plugin.saveConfig();
            plugin.reloadConfig();
            player.sendMessage("JSON сохранен.");
        } catch (Exception e) {
            player.sendMessage("JSON не удалось сохранить. Причина: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
