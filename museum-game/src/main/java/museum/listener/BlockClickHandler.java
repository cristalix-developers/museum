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
import museum.util.MessageUtil;

/**
 * @author func 14.07.2020
 * @project museum
 */
@AllArgsConstructor
public class BlockClickHandler implements Listener {

	private static final PotionEffect INVISIBLE =
			new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false);

	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {
		val block = event.getClickedBlock();
		if (block == null)
			return;
		val location = block.getLocation();
		val player = event.getPlayer();
		val blockType = block.getType();

		if (blockType == Material.PISTON_EXTENSION) {
			Bat bat = (Bat) location.getWorld().spawnEntity(location.clone().add(.5, .2, .5), EntityType.BAT);
			bat.setAI(false);
			bat.addPotionEffect(INVISIBLE);
			bat.addPassenger(player);
		}
	}
}
