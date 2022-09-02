package museum.visitor;

import lombok.Getter;
import lombok.val;
import net.minecraft.server.v1_12_R1.EntityVillager;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

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
			this.goalSelector.a(1, new PathfinderGoalGuide(visitorGroup));
			return;
		}

		//this.goalSelector.a(1, new PathfinderGoalFollowGuide(this, 0.55, 7, 5));
	}

}
