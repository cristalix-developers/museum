package museum.fragment;

import lombok.Getter;
import lombok.val;
import me.func.mod.conversation.data.LootDrop;
import me.func.protocol.data.rare.DropRare;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author func 14.02.2021
 * @project museum
 */
@Getter
public class Gem extends LootDrop implements Fragment {

	private final GemType type;
	private final float rarity;
	private final int realPrice;
	private final UUID uuid;
	private int price;
	private ItemStack item;

	public Gem(String data) {
		super(new ItemStack(Material.CLAY_BALL), "", DropRare.RARE);

		String[] strings = data.split("\\:");
		this.type = GemType.valueOf(strings[0]);
		this.rarity = Float.parseFloat(strings[1]);
		this.price = Integer.parseInt(strings[2]);
		this.uuid = UUID.randomUUID();
		this.realPrice = getPrice();

		val item = CraftItemStack.asNMSCopy(new ItemStack(Material.CLAY_BALL));

		item.tag = new NBTTagCompound();
		item.tag.setString("relic-uuid", uuid.toString());
		item.tag.setString("relic", type.getTexture());
		item.tag.setString("museum", type.getTexture());
		item.tag.setInt("gem", price);
		item.tag.setInt("price", realPrice);


		this.item = item.asBukkitMirror();

		val meta = this.item.getItemMeta();

		meta.setDisplayName("§f" + type.getTitle() + " §f" + Math.round(rarity * 100) + "%");
		meta.setLore(generateLore());

		this.item.setItemMeta(meta);

		super.setItemStack(this.item);
		super.setTitle("§f" + type.getTitle() + " §f" + Math.round(rarity * 100) + "%");
	}

	@Override
	public String getAddress() {
		return type.name() + ":" + rarity + ":" + price;
	}

	@Override
	public int getPrice() {
		return Math.round(rarity * 53 * type.getMultiplier()) * 100 / 2;
	}

	public void setPrice(int price) {
		this.price = price;

		val item = CraftItemStack.asNMSCopy(this.item);
		item.tag.setInt("gem", price);
		this.item = CraftItemStack.asCraftMirror(item);

		val meta = this.item.getItemMeta();
		meta.setLore(generateLore());
		this.item.setItemMeta(meta);
	}

	public List<String> generateLore() {
		return Arrays.asList(
				"",
				"§fЦена камня §a" + MessageUtil.toMoneyFormat(price),
				"§fПрибыток §b~" + Math.round(realPrice / 100D) + "~$",
				"§fРедкость §b" + Math.round(rarity * 100) + "%",
				"",
				"§7Можно продать перекупщику",
				"§7Изменить цену с /gemstat [ЦЕНА]",
				"",
				"§7Поставьте камень на витрину для",
				"§7реликвий, которую вы можете купить",
				"§7в магазине построек."
		);
	}
}