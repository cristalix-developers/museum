package museum.command;

import clepto.bukkit.B;
import lombok.val;
import museum.App;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Managers;
import org.bukkit.Bukkit;

/**
 * @author func 25.08.2020
 * @project museum
 */
public class AdminCommand {
	public static void init(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/money [Игрок] [Количество денег]";
			val player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return null;
			app.getUser(player).setMoney(Double.parseDouble(args[1]));
			return "§aВаше количество денег изменено.";
		}, "money");

		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/exp [Игрок] [Опыт] ИЛИ /exp [Опыт]";
			if (args.length == 1)
				app.getUser(sender).giveExperience(Integer.parseInt(args[0]));
			else if (args.length == 2)
				app.getUser(Bukkit.getPlayer(args[0])).giveExperience(Integer.parseInt(args[1]));
			return "§aОпыт изменен.";
		}, "exp");

		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/dino [Динозавр]";
			SkeletonPrototype proto = Managers.skeleton.getPrototype(args[0]);
			if (proto == null)
				return "§cПрототип динозавра §e" + args[0] + "§c не найден.";
			app.getUser(sender).getSkeletons().supply(proto).getUnlockedFragments().addAll(proto.getFragments());
			return "";
		}, "dino");
	}
}
