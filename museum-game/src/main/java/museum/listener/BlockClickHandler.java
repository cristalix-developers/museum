package museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import museum.App;
import museum.museum.map.SubjectPrototype;
import museum.prototype.Managers;
import museum.ticker.detail.PresentHandler;
import museum.util.MessageUtil;

/**
 * @author func 14.07.2020
 * @project museum
 */
@AllArgsConstructor
public class BlockClickHandler implements Listener {

	private static final PotionEffect INVISIBLE =
			new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false);
	private final App app;
	private final PresentHandler presentHandler;

	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {
		val block = event.getClickedBlock();
		if (block == null)
			return;
		val location = block.getLocation();
		val player = event.getPlayer();
		val user = app.getUser(player);
		if (block.getType() == Material.SIGN_POST) {
			for (SubjectPrototype subjectPrototype : Managers.subject.getMap().values()) {
				if (subjectPrototype.getBox().contains(location)) {
					// todo: открытие инвентаря для данного Subject
					break;
				}
			}
		} else if (block.getType() == Material.PISTON_EXTENSION) {
			Bat bat = (Bat) location.getWorld().spawnEntity(location.clone().add(.5, .2, .5), EntityType.BAT);
			bat.setAI(false);
			bat.addPotionEffect(INVISIBLE);
			bat.addPassenger(player);
		} else if (block.getType() == Material.SKULL) {
			val reward = presentHandler.getPresentByLocation(location);
			if (reward == null)
				return;
			reward.remove();
			val type = reward.getType();
			MessageUtil.find("reward")
					.set("reward", MessageUtil.toMoneyFormat(type.getPrice()))
					.send(user);
			user.setMoney(user.getMoney() + type.getPrice());
			type.getOnFind().onFind(user, location);
		}
	}
}
