package museum.command;

import clepto.bukkit.B;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.data.PickaxeType;
import museum.fragment.Fragment;
import museum.fragment.Gem;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.subject.Allocation;
import museum.museum.subject.RelicShowcaseSubject;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author func 25.08.2020
 * @project museum
 */
@UtilityClass
public class AdminCommand {

	public void init(App app) {
		registerMoneyCmd(app);
		registerCrystalCmd(app);
		registerExpCmd(app);
		registerDinoCmd(app);
		registerPickaxeCmd(app);
		registerPrefixCmd(app);
		registerRunTopCmd(app);
		registerSubjectsCmd(app);
		registerSkeletonCmd(app);
		registerVisitCmd(app);
		registerGetDataCmd(app);
	}

	private void registerGetDataCmd(App app) {
		B.regCommand((sender, args) -> {

			if (!sender.isOp())
				return null;

			if (args.length != 1)
				return "§bИспользование: §c/get_data [Игрок]";

			val player = Bukkit.getPlayer(args[0]);
			val user = app.getUser(player);

			List<String> allGemsInInventory = new ArrayList<>();
			for (Fragment fragment : user.getRelics().values())
				if (fragment instanceof Gem)
					allGemsInInventory.add(fragment.getAddress());
			
			player.sendMessage("§bВсе реликвии у игрока в инвентаре §c" + user.getDisplayName() + "§b:§r");
			for (String relicData : allGemsInInventory)
				player.sendMessage(relicData + "\n");

			List<String> allStandsWithGem = new ArrayList<>();
			val a1 = user.getSubjects();
			for (Subject dataForClient : a1)
				if (dataForClient instanceof RelicShowcaseSubject) {
					val relicCase = ((RelicShowcaseSubject) dataForClient);
					val coords = ((RelicShowcaseSubject) dataForClient).getAbsoluteLocation();
					try {
						allStandsWithGem.add("\nIncome: §c" + relicCase.getIncome() + "§r\n" +
											"Relic: §c" + relicCase.getFragment().getAddress() + "§r");
					} catch (Exception ignored) { }
				}

			player.sendMessage("§bВсе стенды у игрока §c" + user.getDisplayName() + "§b с чем-либо:§r");
			for (String standData : allStandsWithGem)
				player.sendMessage((standData) + "\n");

			return null;
		}, "get_data");
	}

	private void registerVisitCmd(App app) {
		B.regCommand((sender, args) -> {
			val user = app.getUser(sender);

			if (!sender.isOp())
				return null;

			if (args.length <= 1)
				return "§cИспользование: §f/museum visit [Игрок] [Музей]";

			val ownerPlayer = Bukkit.getPlayer(args[1]);

			if (ownerPlayer == null || !ownerPlayer.isOnline())
				return MessageUtil.get("playeroffline");

			val ownerUser = app.getUser(ownerPlayer);
			String address = args.length > 2 ? args[2] : "main";

			MuseumPrototype prototype = Managers.museum.getPrototype(address);
			Museum museum = prototype == null ? null : ownerUser.getMuseums().get(prototype);
			if (museum == null)
				return MessageUtil.get("museum-not-found");

			if (Objects.equals(user.getLastMuseum(), museum))
				return MessageUtil.get("already-at-home");

			user.setState(museum);

			MessageUtil.find("museum-teleported")
					.set("visitor", user.getName())
					.send(ownerUser);
			return null;
		}, "visit", "museum");
	}

	private void registerSubjectsCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			Collection<Subject> subjects = app.getUser(sender).getSubjects();
			for (Subject subject : subjects) {
				String allocationInfo = "§cno allocation";
				Allocation allocation = subject.getAllocation();
				if (allocation != null) {
					Location origin = allocation.getOrigin();
					allocationInfo = allocation.getUpdatePackets().size() + " packets, §f" + origin.getX() + " " + origin.getY() + " " + origin.getZ();
				}
				sender.sendMessage("§e" + subject.getPrototype().getAddress() + "§f: " + subject.getOwner().getName() + ", " + allocationInfo);
			}
			return "§e" + subjects.size() + " in total.";
		}, "subjects", "sj");
	}

	private void registerPrefixCmd(App app) {
		B.regCommand((sender, args) -> {
			if (sender != null && !sender.isOp()) return "§cНеизвестная команда.";
			if (args.length < 2) return "§cИспользование: §e/prefix [Игрок] [Префикс]";
			try {
				User user = app.getUser(Bukkit.getPlayer(args[0]).getUniqueId());
				user.setPrefix(args[1].replace('&', '§').replace('#', '¨'));
				return "§aПрефикс изменён.";
			} catch (NullPointerException ex) {
				return "§cИгрок не найден.";
			}
		}, "prefix");
	}

	private void registerRunTopCmd(App app) {
		B.regCommand((sender, args) -> {
			User user = app.getUser(sender);
			if (!sender.isOp() && user.getLastTopUpdateTime() != 0) return null;
			user.setLastTopUpdateTime(-1);
			return null;
		}, "runtop", "rt");
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
			return "§bТеперь у " + player.getDisplayName() + "§c " + MessageUtil.toMoneyFormat(user.getMoney()) + "§b денег!";
		}, "money");
	}

	private void registerCrystalCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			if (args.length == 0)
				return "§cИспользование: §e/crystal [Игрок] [Количество денег]";
			val player = Bukkit.getPlayer(args[0]);
			if (player == null)
				return null;
			val user = app.getUser(player);
			user.setCosmoCrystal((int) (user.getCosmoCrystal() + Double.parseDouble(args[1])));
			return "§bТеперь у " + player.getDisplayName() + "§c " + MessageUtil.toCrystalFormat(user.getCosmoCrystal()) + "§b кристаллов!";
		}, "crystal");
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

	private void registerSkeletonCmd(App app) {
		B.regCommand((sender, args) -> {
			if (!sender.isOp())
				return null;
			Collection<Skeleton> skeletons = app.getUser(sender).getSkeletons();
			skeletons.forEach(skeleton ->
					sender.sendMessage("§e" + skeleton.getPrototype().getAddress() + "§f: " + skeleton.getUnlockedFragments().size()));
			return "§e" + skeletons.size() + " in total.";
		}, "skeleton");
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
