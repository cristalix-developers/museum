package ru.func.museum.command;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.func.museum.App;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumCommand implements CommandExecutor {

    private App app;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            val player = (Player) commandSender;

            if (strings.length == 2) {
                if (strings[0].equals("accept")) {
                    val author = Bukkit.getPlayer(strings[1]);

                    if (author == null || !author.isOnline()) {
                        player.sendMessage("§7[§l§bi§7] Игрок, который пригласил вас не в сети. 㬏");
                        return true;
                    }

                    val sender = app.getArchaeologistMap().get(author.getUniqueId());

                    if (app.getArchaeologistMap().get(player.getUniqueId()).getCurrentMuseum().getOwner().equals(sender)) {
                        player.sendMessage("§7[§l§bi§7] Вы уже прибыли!");
                        return true;
                    }

                    sender.getCurrentMuseum().load(app, sender, player);

                    author.sendMessage("§7[§l§bi§7] " + player.getName() + " принял приглашение.");
                }
            }
        }
        return true;
    }
}
