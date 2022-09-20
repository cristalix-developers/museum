package museum.fragment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import me.func.mod.conversation.data.LootDrop;
import me.func.protocol.data.rare.DropRare;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author func 04.05.2021
 * @project museum
 */
@Getter
public class Meteorite extends LootDrop implements Fragment {

	private final Meteorites meteorite;
	private ItemStack cache;
	private final UUID uuid = UUID.randomUUID();

	public Meteorite(String prototypeAddress) {
		super(new ItemStack(), "", DropRare.EPIC);
		meteorite = Meteorites.valueOf(prototypeAddress.split("\\_")[1].toUpperCase());
		super.setItemStack(meteorite.getItem());
		super.setTitle(meteorite.getTitle());
	}

	@Override
	public String getAddress() {
		return "meteor_" + meteorite.name().toLowerCase();
	}

	@Override
	public ItemStack getItem() {
		if (cache == null) {
			val item = CraftItemStack.asNMSCopy(meteorite.getItem());

			item.tag.setString("relic-uuid", uuid.toString());
			item.tag.setInt("price", getPrice());

			cache = item.asBukkitMirror();
		}
		return cache;
	}

	@Override
	public int getPrice() {
		return meteorite.getPrice() * 100;
	}

	@Getter
	@AllArgsConstructor
	public enum Meteorites {
		GIRIN(genItem("⭐⭐ Гирин", Arrays.asList(
				"§fГруппа хондритов общим весом",
				"§fболее 4 тонн, упавших",
				"§fвблизи города Гирин в",
				"§fодноимённой китайской провинции",
				"§fв 1976 году."
		), 20), 20),
		NAHLA(genItem("⭐⭐ Нахла", Arrays.asList(
				"§fПервый известный марсианский",
				"§fметеорит, обнаруженный в",
				"§fЕгипте в 1911 году."
		), 21), 21),
		BASHKUVKA(genItem("⭐ Башкувка", Arrays.asList(
				"§fМетеорит из класса хондритов,",
				"§fупавший 25 августа",
				"§f1994 года в 4:00",
				"§fна территории села Башкувка",
				"§fв 25 км к",
				"§fюго-западу от Варшавы."
		), 9), 9),
		ALLENDE(genItem("⭐⭐ Альенде", Arrays.asList(
				"§fКрупнейший углистый метеорит,",
				"§fнайденный на Земле."
		), 19), 19),
		ANDREEVKA(genItem("⭐ Андреевка", Arrays.asList(
				"§fКаменный метеорит-хондрит",
				"§fвесом 600 граммов."
		), 7), 7),
		HOBA(genItem("⭐⭐⭐ Гоба", Arrays.asList(
				"§fКрупнейший из найденных метеоритов",
				"§fОн является и самым",
				"§fбольшим на Земле куском",
				"§fжелеза природного происхождения."
		), 29), 29),
		ALINSKY(genItem("⭐ Сихотэ-Алинский", Arrays.asList(
				"§fЖелезный метеорит, разрушившийся",
				"§fпри входе в атмосферу",
				"§fи выпавший в виде",
				"§fметеоритного дождя."
		), 6), 6),
		BARBOTAN(genItem("⭐ Барботан", Arrays.asList(
				"§fМетеорит-хондрит.",
				"§fУпал 24 июля 1790 года",
				"§fво Франции, в районе",
				"§fдепартамента Жер."
		), 5), 5),
		COUNTY(genItem("⭐⭐ Нортон Каунти", Collections.singletonList("§fИнформация недоступна"), 18), 18),
		KUNYA(genItem("⭐⭐ Куня-Ургенч", Collections.singletonList("§fМетеорит упал 20 июня 1998 г."), 17), 17),
		MILL(genItem("⭐ Саттерз-Милл", Arrays.asList(
				"§fКрупный метеорит, взорвавшийся",
				"§fнад штатом Вашингтон",
				"§f22 апреля 2012 года с",
				"§fмощностью, аналогичной 4",
				"§fкилотоннам тротила."
		), 8), 8),
		BAHMUT(genItem("⭐ Бахмут", Arrays.asList(
				"§fКаменный метеорит-хондрит",
				"§fвесом 7979 грамм."
		), 7), 7),
		ALFIANELLO(genItem("⭐⭐⭐ Альфиане́лло", Arrays.asList(
				"§fМетеорит-хондрит весом",
				"§f228 000 граммов."
		), 28), 28),
		ABEE(genItem("⭐⭐⭐ Эйби́", Arrays.asList(
				"§fЭнстатитовый метеорит весом",
				"§f107 000 граммов."
		), 27), 27),
		CHELYABA(genItem("⭐ Челябинский", Arrays.asList(
				"§fМетеорит, упавший на земную",
				"§fповерхность 15 февраля 2013 года."
		), 4), 4),
		;

		private final ItemStack item;
		private final int price;

		public String getTitle() {
			return item.getItemMeta().getDisplayName();
		}

		private static ItemStack genItem(String title, List<String> lore, int price) {
			val meteor = CraftItemStack.asNMSCopy(new ItemStack(Material.OBSIDIAN));

			meteor.tag = new NBTTagCompound();
			meteor.tag.setString("relic", title);

			val item = meteor.asBukkitMirror();
			val meta = item.getItemMeta();
			val newLore = new ArrayList<>(Collections.singletonList("  "));

			newLore.addAll(lore);
			newLore.add(" ");
			newLore.add("§fДоход: " + price);
			meta.setLore(newLore);

			val words = title.split("\\s+");
			meta.setDisplayName("§6" + words[0] + " §f" + words[1]);

			item.setItemMeta(meta);
			return item;
		}
	}
}
