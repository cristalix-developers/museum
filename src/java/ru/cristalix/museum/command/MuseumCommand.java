package ru.cristalix.museum.command;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.cristalix.museum.App;
import ru.cristalix.museum.util.MessageUtil;

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
			val user = app.getUser(player.getUniqueId());

			if (strings.length == 2) {
				if (strings[0].equals("accept")) {
					val author = Bukkit.getPlayer(strings[1]);

					if (author == null || !author.isOnline()) {
						MessageUtil.find("playeroffline").send(user);
						return true;
					}
					val userAuthor = app.getUser(author);

					val sender = app.getUser(author.getUniqueId());

					if (user.getCurrentMuseum().getOwner().equals(sender))
						return true;
					//todo музей не загружается
					sender.getCurrentMuseum().unload(user);
					userAuthor.getCurrentMuseum().load(user);

					MessageUtil.find("visitaccept")
							.set("visitor", player.getName())
							.send(sender);
				}
			}
		}
		return true;
	}

}
