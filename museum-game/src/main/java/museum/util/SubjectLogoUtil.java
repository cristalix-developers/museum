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

	private static final String SECURITY_FIELD_NAME = "subject-uuid";

	public static ItemStack encodeSubjectToItemStack(Subject subject) {
		val itemProto = subject.getPrototype().getIcon();
		val nmsItem = CraftItemStack.asNMSCopy(itemProto);
		val nbtTagCompound = nmsItem.getTag() != null ? nmsItem.getTag() : new NBTTagCompound();
		nbtTagCompound.setString(SECURITY_FIELD_NAME, String.valueOf(subject.getCachedInfo().getUuid()));
		nmsItem.setTag(nbtTagCompound);
		return CraftItemStack.asBukkitCopy(nmsItem);
	}

	public static Subject decodeItemStackToSubject(User user, ItemStack itemStack) {
		if (itemStack == null)
			return null;

		val nmsCopy = CraftItemStack.asNMSCopy(itemStack);
		val tag = nmsCopy.getTag();

		if (tag == null || !tag.hasKeyOfType(SECURITY_FIELD_NAME, 8))
			return null;

		return user.getSubject(UUID.fromString(tag.getString(SECURITY_FIELD_NAME)));
	}
}
