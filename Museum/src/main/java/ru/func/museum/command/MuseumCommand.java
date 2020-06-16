package ru.func.museum.command;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.util.MessageUtil;

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
                        MessageUtil.find("playeroffline").send(player);
                        return true;
                    }

                    val sender = app.getArchaeologistMap().get(author.getUniqueId());

                    if (app.getArchaeologistMap().get(player.getUniqueId()).getCurrentMuseum().getOwner().equals(sender))
                        return true;

                    sender.getCurrentMuseum().unload(app, sender, player);
                    sender.getCurrentMuseum().load(app, sender, player);

                    MessageUtil.find("visitaccept")
                            .set("visitor", player.getName())
                            .send(author);
                }
            }
        }
        return true;
    }
}
