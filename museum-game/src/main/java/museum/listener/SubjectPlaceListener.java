package museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import museum.App;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author func 09.09.2020
 * @project museum
 */
@AllArgsConstructor
public class SubjectPlaceListener implements Listener {

	private final App app;
	private final static ItemStack AIR_ITEM = new ItemStack(Material.AIR);

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(true);

		val player = event.getPlayer();
		val user = app.getUser(player);

		if (user.getCurrentMuseum() == null)
			return;

		val item = player.getInventory().getItemInMainHand();
		val subject = SubjectLogoUtil.decodeItemStackToSubject(user, item);

		if (subject == null)
			return;

		player.getInventory().setItemInMainHand(AIR_ITEM);
		subject.allocate(event.getBlockPlaced().getLocation());
		subject.show(user);
		MessageUtil.find("placed").send(user);
	}
}
