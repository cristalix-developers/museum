package museum.visitor;

import lombok.Getter;
import lombok.val;
import museum.App;
import net.minecraft.server.v1_12_R1.EntityVillager;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.UUID;

@Getter
public class EntityVisitor extends EntityVillager {

	public EntityVisitor(org.bukkit.World world, VisitorGroup group) {
		super(((CraftWorld) world).getHandle());
		VisitorHandler.getVisitorUuids().put(uniqueID, group);
	}

	@Override
	public void r() {
		val uuid = VisitorHandler.getVisitorUuids();
		if (uuid.isEmpty() || getUniqueID() == null)
			return;
		if (!uuid.containsKey(getUniqueID()))
			return;

		val visitorGroup = uuid.get(getUniqueID());
		if (visitorGroup.getGuide().equals(this)) {
			this.goalSelector.a(1, new PathfinderGoalGotoStand(visitorGroup));
			return;
		}

		this.goalSelector.a(1, new PathfinderGoalFollowGuide(this, 0.55, 3, 4));
	}

}
