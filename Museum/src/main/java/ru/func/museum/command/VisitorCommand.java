package ru.func.museum.command;

import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.func.museum.visitor.VisitorManager;

/**
 * @author func 09.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class VisitorCommand implements CommandExecutor {

    private VisitorManager visitorManager;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.isOp()) {
            switch (strings[0]) {
                case "reload":
                    visitorManager.clear(); // IF RELOAD - DO NOT BREAK!!!
                case "spawn":
                    visitorManager.spawn(((Player) commandSender).getLocation());
                    break;
                case "clear":
                    visitorManager.clear();
                    break;
            }
        }
        return true;
    }
}
