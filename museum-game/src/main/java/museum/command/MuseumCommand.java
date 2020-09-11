package museum.command;

import clepto.bukkit.B;
import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.museum.Museum;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Managers;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@AllArgsConstructor
public class MuseumCommand implements B.Executor {

	private final App app;

	@Override
	public String execute(Player sender, String[] args) {
		if (sender == null) return "Only for players.";
		val visitorUser = app.getUser(sender);

		if (args.length == 0)
			return "§cИспользование: §f/museum [accept]";

		if (args[0].equals("accept")) {
			if (args.length < 2)
				return "§cИспользование: §f/museum accept Игрок";
			val ownerPlayer = Bukkit.getPlayer(args[1]);

			if (ownerPlayer == null || !ownerPlayer.isOnline())
				return MessageUtil.get("playeroffline");

			val ownerUser = app.getUser(ownerPlayer);

			Museum museum = ownerUser.getCurrentMuseum();
			if (museum.getOwner() != ownerUser)
				return MessageUtil.get("playerbusy");

			if (ownerUser.equals(visitorUser))
				return MessageUtil.get("inviteyourself");

			visitorUser.getCurrentMuseum().hide(visitorUser);
			museum.show(visitorUser);

			return MessageUtil.find("visitaccept")
					.set("visitor", visitorUser.getName())
					.getText();
		}

		if (args[0].equals("dino")) {
			if (args.length < 2) return "§cИспользование: §e/museum dino [Адрес скелета] (Адрес фрагмента)";
			SkeletonPrototype proto = Managers.skeleton.getPrototype(args[1]);
			if (proto == null) return "§cДинозавр §e" + args[1] + "§c не найден. Доступные: §e" +
					String.join(", ", Managers.skeleton.getMap().keySet());

			Location location = sender.getEyeLocation().add(sender.getLocation().getDirection().multiply(2));
			if (args.length > 2) {
				StringBuilder sb = new StringBuilder();
				for (int i = 2; i < args.length; i++) {
					if (i > 2) sb.append(' ');
					sb.append(args[i]);
				}
				String fragmentAddress = sb.toString();
				Optional<Fragment> opt = proto.getFragments().stream().filter(a -> a.getAddress().equalsIgnoreCase(fragmentAddress)).findAny();
				if (!opt.isPresent()) return "§cКость §e" + fragmentAddress + "§c не найдена в скелете §e" + proto.getAddress();
				Fragment fragment = opt.get();
				fragment.hide(sender);
				fragment.show(sender, location);
				return "§aФрагмент §e" + proto.getTitle() + "/" + fragment.getAddress() + "§a отображён рядом с вами.";
			} else {
				proto.hide(sender);
				proto.show(sender, location);
				return "§aДинозавр §e" + proto.getTitle() + "§a отображён рядом с вами.";
			}
		}
		return "§cНеизвестная подкоманда.";
	}
}
