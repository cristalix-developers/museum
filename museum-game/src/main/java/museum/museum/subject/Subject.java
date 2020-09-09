package museum.museum.subject;

import clepto.bukkit.B;
import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.util.UtilV3;
import museum.App;
import museum.prototype.Storable;
import museum.data.SubjectInfo;
import museum.museum.map.SubjectPrototype;
import museum.player.User;

/**
 * @author func 22.05.2020
 * @project Museum
 */
@Getter
public class Subject extends Storable<SubjectInfo, SubjectPrototype> {

	private Allocation allocation;

	public Subject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		B.run(() -> allocate(UtilV3.toLocation(info.getLocation(), App.getApp().getWorld())));
	}

	public Allocation allocate(Location origin) {
		if (origin == null)
			System.out.println("Clearing allocation for " + prototype.getAddress());
		return this.allocation = Allocation.allocate(cachedInfo, prototype, origin);
	}

	public boolean isAllocated() {
		return allocation != null;
	}

	public void show(User user) {
		if (allocation != null) allocation.getShowPackets().forEach(user::sendPacket);
	}

	public void hide(User user) {
		if (allocation == null)
			return;
		allocation.getHidePackets().forEach(user::sendPacket);
	}

	public double getIncome() {
		return 0;
	}

}
