package museum.command;

import clepto.bukkit.B;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Managers;
import org.bukkit.Bukkit;

/**
 * @author func 25.08.2020
 * @project museum
 */
@UtilityClass
public class AdminCommand {

	public void init(App app) {
		registerMoneyCmd(app);
		registerExpCmd(app);
		registerDinoCmd(app);
		registerPickaxeCmd(app);
	}

	private void registerMoneyCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/money [Игрок] [Количество денег]";
			val player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return null;
			val user = app.getUser(player);
			user.setMoney(user.getMoney() + Double.parseDouble(args[1]));
			return "§aКоличество денег изменено.";
		}, "money");
	}

	private void registerPickaxeCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			val player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return null;
			app.getUser(player).setPickaxeType(PickaxeType.LEGENDARY);
			return "§aКирка выдана.";
		}, "returnpickaxe");
	}

	private void registerExpCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/exp [Игрок] [Опыт] ИЛИ /exp [Опыт]";
			try {
				if (args.length == 1)
					app.getUser(sender).giveExperience(Integer.parseInt(args[0]));
				else if (args.length == 2)
					app.getUser(Bukkit.getPlayer(args[0])).giveExperience(Integer.parseInt(args[1]));
				return "§aОпыт изменен.";
			} catch (Exception exception) {
				return exception.getMessage();
			}
		}, "exp");
	}

	private void registerDinoCmd(App app) {
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
