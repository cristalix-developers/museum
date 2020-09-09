package museum.util;

import lombok.val;
import museum.museum.subject.Subject;
import museum.player.User;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * @author func 09.09.2020
 * @project museum
 */
public class SubjectLogoUtil {

	public static ItemStack encodeSubjectToItemStack(Subject subject) {
		val itemProto = subject.getPrototype().getLogo().clone();

		val nbtTagCompound = new NBTTagCompound();
		nbtTagCompound.setUUID("uuid", subject.getCachedInfo().getUuid());

		val nmsItem = CraftItemStack.asNMSCopy(itemProto);
		nmsItem.setTag(nbtTagCompound);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	public static Subject decodeItemStackToSubject(User user, ItemStack itemStack) {
		if (itemStack == null || itemStack.getItemMeta() == null)
			return null;

		val nmsCopy = CraftItemStack.asNMSCopy(itemStack);
		val tag = nmsCopy.getTag();

		if (tag == null || !tag.hasKey("uuid"))
			return null;

		return user.getCurrentMuseum().getSubjectByUuid(tag.getUUID("uuid"));
	}
}
