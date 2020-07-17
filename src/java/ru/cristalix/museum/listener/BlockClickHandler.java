package ru.cristalix.museum.listener;

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
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.prototype.Managers;
import ru.cristalix.museum.ticker.detail.HeadRewardHandler;
import ru.cristalix.museum.util.MessageUtil;

/**
 * @author func 14.07.2020
 * @project museum
 */
@AllArgsConstructor
public class BlockClickHandler implements Listener {

	private static final PotionEffect INVISIBLE =
			new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 200000, 1, false, false);
	private final App app;
	private final HeadRewardHandler headRewardHandler;

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
			val reward = headRewardHandler.getHeadRewardByLocation(location);
			if (reward == null)
				return;
			val price = reward.getStatus().getPrice();
			MessageUtil.find("reward")
					.set("reward", MessageUtil.toMoneyFormat(price))
					.send(user);
			user.setMoney(user.getMoney() + price);
			reward.getStatus().getOnFind().onFind(user, location);
			reward.remove();
		}
	}
}
