package museum.fragment;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author func 14.02.2021
 * @project museum
 */
@Getter
public class Gem implements Fragment {

	private final GemType type;
	private final float rarity;
	private final int price;
	private final ItemStack item;
	private final UUID uuid;

	public Gem(String data) {
		String[] strings = data.split("\\:");
		this.type = GemType.valueOf(strings[0]);
		this.rarity = Float.parseFloat(strings[1]);
		this.price = Integer.parseInt(strings[2]);
		this.uuid = UUID.randomUUID();

		val item = CraftItemStack.asNMSCopy(new ItemStack(Material.CLAY_BALL));

		item.tag = new NBTTagCompound();
		item.tag.setString("relic-uuid", uuid.toString());
		item.tag.setString("relic", type.getTexture());
		item.tag.setString("museum", type.getTexture());
		item.tag.setInt("price", (int) rarity * 96);

		this.item = item.asBukkitMirror();

		val meta = this.item.getItemMeta();

		meta.setDisplayName("§f" + type.getTitle() + " §f" + Math.round(rarity * 100) + "%");
		meta.setLore(Arrays.asList(
				"§fЦена камня §a" + price + "$",
				"§fПрибыток §b~" + Math.round(rarity * 96) + "~$",
				"§fРедкость §b" + Math.round(rarity * 100) + "%",
				"",
				"§7Можно продать перекупщику",
				"",
				"§7Поставьте камень на витрину для",
				"§7реликвий, которую вы можете купить",
				"§7в магазине построек."
		));
		this.item.setItemMeta(meta);
	}

	@Override
	public String getAddress() {
		return type.name() + ":" + rarity + ":" + price;
	}
}