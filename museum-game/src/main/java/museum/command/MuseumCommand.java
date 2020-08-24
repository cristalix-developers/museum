package museum.command;

import clepto.bukkit.B;
import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumCommand implements B.Executor {

	private App app;

	@Override
	public String execute(Player sender, String[] args) {
		if (sender == null) return "Only for players.";
		val user = app.getUser(sender);

		if (args.length == 0)
			return "§cИспользование: §f/museum [accept]";

		if (args[0].equals("accept")) {
			if (args.length < 2)
				return "§cИспользование: §f/museum accept Игрок";
			val visitorPlayer = Bukkit.getPlayer(args[1]);

			if (visitorPlayer == null || !visitorPlayer.isOnline())
				return MessageUtil.find("playeroffline").getText();

			val visitorUser = app.getUser(visitorPlayer);

			if (user.getCurrentMuseum().getOwner().equals(user))
				return "§eПригласил сам себя?";

			visitorUser.getCurrentMuseum().hide(visitorUser);
			user.getCurrentMuseum().show(visitorUser);

			return MessageUtil.find("visitaccept")
					.set("visitor", user.getName()).getText();
		}

		return "§cНеизвестная подкоманда.";

	}

}
