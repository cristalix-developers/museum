package museum.util;

import lombok.val;
import museum.museum.subject.Subject;
import museum.player.User;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * @author func 09.09.2020
 * @project museum
 */
public class SubjectLogoUtil {

	public static ItemStack encodeSubjectToItemStack(Subject subject) {
		val itemProto = subject.getPrototype().getIcon().render();
		val nmsItem = CraftItemStack.asNMSCopy(itemProto);
		val nbtTagCompound = nmsItem.getTag() != null ? nmsItem.getTag() : new NBTTagCompound();
		nbtTagCompound.setString("subject-uuid", String.valueOf(subject.getCachedInfo().getUuid()));
		nmsItem.setTag(nbtTagCompound);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	public static Subject decodeItemStackToSubject(User user, ItemStack itemStack) {
		if (itemStack == null)// || itemStack.getItemMeta() == null)
			return null;

		val nmsCopy = CraftItemStack.asNMSCopy(itemStack);
		val tag = nmsCopy.getTag();

		if (tag == null || !tag.hasKey("subject-uuid"))
			return null;

		return user.getSubject(UUID.fromString(tag.getString("subject-uuid")));
	}
}
