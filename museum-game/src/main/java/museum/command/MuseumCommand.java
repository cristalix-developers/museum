package museum.command;

import clepto.bukkit.B;
import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.museum.Museum;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.museum.subject.skeleton.V4;
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
		val user = app.getUser(sender);

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

			if (ownerUser.equals(user))
				return MessageUtil.get("inviteyourself");

			user.getCurrentMuseum().hide(user);
			museum.show(user);

			return MessageUtil.find("visitaccept")
					.set("visitor", user.getName())
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
				fragment.hide(user);
				fragment.show(user, V4.fromLocation(location));
				return "§aФрагмент §e" + proto.getTitle() + "/" + fragment.getAddress() + "§a отображён рядом с вами.";
			} else {
				proto.hide(user);
				proto.show(user, V4.fromLocation(location));
				return "§aДинозавр §e" + proto.getTitle() + "§a отображён рядом с вами.";
			}
		}
		return "§cНеизвестная подкоманда.";
	}
}
