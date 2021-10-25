package museum.player;

import clepto.bukkit.B;
import clepto.bukkit.LocalArmorStand;
import clepto.bukkit.event.PlayerWrapper;
import lombok.Data;
import lombok.experimental.Delegate;
import lombok.val;
import museum.App;
import museum.boosters.BoosterType;
import museum.client_conversation.AnimationUtil;
import museum.data.*;
import museum.fragment.Fragment;
import museum.fragment.Gem;
import museum.fragment.Meteorite;
import museum.fragment.Relic;
import museum.museum.Museum;
import museum.museum.map.MuseumPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.Subject;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.prototype.Managers;
import museum.prototype.Registry;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.AsyncCatcher;
import ru.cristalix.core.util.UtilV3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class User implements PlayerWrapper {

	@Delegate
	private final UserInfo info;
	private final Registry<MuseumInfo, MuseumPrototype, Museum> museums
			= new Registry<>(this, Managers.museum, MuseumInfo::new, Museum::new);
	private final Registry<SkeletonInfo, SkeletonPrototype, Skeleton> skeletons
			= new Registry<>(this, Managers.skeleton, SkeletonInfo::new, Skeleton::new);
	private final Registry<SubjectInfo, SubjectPrototype, Subject> subjects
			= new Registry<>(this, Managers.subject, SubjectInfo::generateNew, SubjectPrototype::provide);
	private final List<Fragment> relics;
	private CraftPlayer player;
	private PlayerConnection connection;
	private Location lastLocation;
	private State state;
	private LocalArmorStand grabbedArmorstand;
	private EntityArmorStand riding;
	private long enterTime;
	private long lastTopUpdateTime = -1;

	public User(UserInfo info) {
		this.enterTime = System.currentTimeMillis();
		this.info = info;

		this.skeletons.importInfos(info.getSkeletonInfos());
		this.subjects.importInfos(info.getSubjectInfos());
		this.museums.importInfos(info.getMuseumInfos());
		List<Fragment> list = new ArrayList<>();
		for (String address : info.getClaimedRelics()) {
			Fragment fragment;
			if (address.contains(":"))
				fragment = new Gem(address);
			else if (address.contains("meteor"))
				fragment = new Meteorite(address);
			else
				fragment = new Relic(address);
			list.add(fragment);
		}
		this.relics = list;

		if (info.getLastPosition() != null)
			this.lastLocation = UtilV3.toLocation(info.getLastPosition(), App.getApp().getWorld());
		else
			this.lastLocation = Managers.museum.getPrototype("main").getSpawn();

		this.state = this.museums.get(Managers.museum.getPrototype("main"));

		B.postpone(50, this::updateIncome);
	}

	public void hideFromAll() {
		val app = App.getApp();
		// Отправка таба
		val show = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.getHandle());
		// Скрытие игроков
		for (Player current : Bukkit.getOnlinePlayers()) {
			if (current == null)
				continue;
			player.hidePlayer(app, current.getPlayer());
			sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) current).getHandle()));
			current.hidePlayer(app, player);
			((CraftPlayer) current).getHandle().playerConnection.sendPacket(show);
		}
	}

	public void showToAllState() {
		val app = App.getApp();
		for (User current : app.getUsers()) {
			if (current.getConnection() == null)
				continue;
			if (current.getState() == this.state) {
				current.getPlayer().showPlayer(app, player);
				player.showPlayer(app, current.getPlayer());
			}
		}
	}

	public void sendTitle(String title) {
		AnimationUtil.title(this, title);
	}

	public void setState(State state) {
		AsyncCatcher.catchOp("Async state change");
		if (this.state != null && this.state != state)
			this.state.leaveState(this);
		val previousState = this.state;
		this.state = state;
		state.enterState(this);
		if (state.nightVision()) {
			player.addPotionEffect(State.NIGHT_VISION);
		} else {
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
		}
		// Если видимость прошлого и нового state не совпадают - скрыть/показать игроков
		B.postpone(1, () -> {
			if (state.playerVisible() && !previousState.playerVisible()) {
				showToAllState();
			} else if (!state.playerVisible()) {
				hideFromAll();
			}
		});
	}

	public Subject getSubject(UUID uuid) {
		for (Subject subject : this.subjects)
			if (subject.getCachedInfo().getUuid().equals(uuid))
				return subject;
		return null;
	}

	public void giveExperience(double exp) {
		int prevLevel = getLevel();
		info.experience += exp * App.getApp().getPlayerDataManager().calcMultiplier(getUuid(), BoosterType.EXP);
		int newLevel = getLevel();
		if (newLevel != prevLevel) {
			AnimationUtil.glowing(this, 42, 189, 102);
			if (newLevel % 50 == 0) {
				TextComponent message = new TextComponent("" +
						"§cВНИМАНИЕ! §e" + getName() +
						" достигнул уровня §b" + newLevel +
						"§e, нажмите §e§lСЮДА§e что бы поздравить!"
				);
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/congr " + getName()));
				for (User user : App.getApp().getUsers())
					if (user.getPlayer() != null)
						user.getPlayer().sendMessage(message);
			}
			if (newLevel > 49) {
				AnimationUtil.topTitle(this, "㿸 Вы достигли §b%d уровня§f, ваша награда §6§l1`000$§f", newLevel);
				setMoney(getMoney() + 1000);
			} else {
				MessageUtil.find("levelup")
						.set("level", newLevel)
						.set("exp", LevelSystem.getRequiredExperience(newLevel) - getExperience())
						.send(this);
			}
		}
		AnimationUtil.updateLevelBar(this);
	}

	public int getLevel() {
		return LevelSystem.getLevel(getExperience());
	}

	public UserInfo generateUserInfo() {
		updateIncome();
		info.museumInfos = museums.getData();
		info.skeletonInfos = skeletons.getData();
		info.subjectInfos = subjects.getData();
		List<String> list = new ArrayList<>();
		for (Fragment relic : relics) {
			String prototypeAddress = relic.getAddress();
			list.add(prototypeAddress);
		}
		info.setClaimedRelics(list);
		return info;
	}

	public void sendPacket(Packet<?> packet) {
		connection.sendPacket(packet);
	}

	public double calcMultiplier(BoosterType type) {
		info.getLocalBoosters().removeIf(BoosterInfo::hadExpire);

		double sum = 1;
		for (BoosterInfo booster : info.getLocalBoosters()) {
			if (booster.getType() == type) {
				sum += booster.getMultiplier() - 1;
			}
		}
		return sum;
	}

	@Override
	public String toString() {
		return this.getDisplayName();
	}

	public Museum getLastMuseum() {
		for (Museum museum : museums)
			if (museum.getPrototype().getBox().contains(lastLocation))
				return museum;
		return museums.get(Managers.museum.getPrototype("main"));
	}

	public void updateIncome() {
		setIncome(0);
		for (Museum museum : getMuseums())
			setIncome(getIncome() + museum.getIncome());
		AnimationUtil.updateIncome(this);
	}

	public void depositMoneyWithBooster(double income) {
		giveMoney(income * App.getApp().getPlayerDataManager().calcMultiplier(getUuid(), BoosterType.COINS));
	}

	public void giveMoney(double money) {
		setMoney(getMoney() + money);
		AnimationUtil.updateMoney(this);
	}

	public void giveCosmoCrystal(int crystal) {
		setCosmoCrystal(getCosmoCrystal() + crystal);
		AnimationUtil.cursorHighlight(this, "§b§l"+ (crystal > 0 ? "+" : "-") + crystal + " §fкристалл");
		AnimationUtil.updateCosmoCrystal(this);
	}
}