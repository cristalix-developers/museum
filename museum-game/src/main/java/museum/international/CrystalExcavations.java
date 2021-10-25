package museum.international;

import clepto.bukkit.B;
import clepto.bukkit.item.Items;
import com.google.common.collect.Maps;
import implario.ListUtils;
import lombok.val;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.fragment.GemType;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author func 25.11.2020
 * @project museum
 */
public class CrystalExcavations implements International {
	private final Map<GemType, List<Location>> spawnPoints = Maps.newHashMap();
	private final ItemStack hook = Items.render("hook").asBukkitMirror();
	private final ItemStack ore = Items.render("ore").asBukkitMirror();

	public CrystalExcavations(App app) {
		app.getMap().getLabels("mine").forEach(mine -> {
			for (GemType type : GemType.values()) {
				if (mine.getTag().toUpperCase().equals(type.name())) {
					if (!spawnPoints.containsKey(type))
						spawnPoints.put(type, new ArrayList<>());
					spawnPoints.get(type).add(mine);
					break;
				}
			}
		});
	}

	@Override
	public void enterState(User user) {
		val player = user.getPlayer();
		player.setAllowFlight(false);
		player.setFlying(false);

		val actual = GemType.getActualGem();

		if (!spawnPoints.containsKey(actual)) {
			throw new RuntimeException("Cannot find spawn points for " + actual.name());
		}

		user.teleport(ListUtils.random(spawnPoints.get(actual)));
		val inventory = user.getInventory();
		inventory.clear();
		// Предметы кешируются
		val userHook = hook.clone();
		val hookMeta = userHook.getItemMeta();
		val hookLevel = user.getInfo().getHookLevel();
		if (hookLevel > 1)
			hookMeta.addEnchant(Enchantment.LURE, hookLevel - 1, true);
		hookMeta.setDisplayName(hookMeta.getDisplayName() + " §fУР. " + hookLevel);
		userHook.setItemMeta(hookMeta);

		inventory.addItem(Items.render(user.getPickaxeType().name().toLowerCase()).asBukkitMirror(), userHook);
		inventory.setItem(8, BeforePacketHandler.EMERGENCY_STOP);

		user.sendTitle("§7Прибытие!\n\n§bударяйте камни");
		user.sendMessage(
				"⟼  §6§l" + actual.getLocation(),
				"",
				"    §bУдаряйте камни §fв земле.",
				"  Промывайте полученную руду",
				"  §bнажимая SHIFT §fоколо водопромывающей",
				"  станции, взрывай рудники динамитом.",
				"  Лагает? Скрыть/показать игроков /hide и /show "
		);
	}

	@Override
	public void leaveState(User user) {
	}

	@Override
	public boolean playerVisible() {
		return true;
	}

	@Override
	public boolean nightVision() {
		return false;
	}

	@Override
	public void acceptBlockBreak(User user, PacketPlayInBlockDig packet) {
		if (packet.c == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
			val location = new Location(user.getWorld(), packet.a.getX(), packet.a.getY(), packet.a.getZ());

			if (location.distanceSquared(user.getLocation()) > 29)
				return;

			val block = location.getBlock();

			if (block != null && block.getType() == Material.STAINED_GLASS) {
				block.setType(Material.AIR);
				user.getInventory().addItem(ore);
				AnimationUtil.cursorHighlight(user, "§d§l+1 §fруда");
				B.postpone(50, () -> {
					block.setType(Material.STAINED_GLASS);
					block.setData((byte) 10);
				});
			}
		}
	}

	@Override
	public boolean canBeBroken(BlockPosition pos) {
		return false;
	}
}