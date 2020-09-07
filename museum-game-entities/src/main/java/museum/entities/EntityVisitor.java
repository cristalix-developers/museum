package museum.entities;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

@Getter
public class EntityVisitor extends EntityVillager {

	private VisitorGroup visitorGroup;

	public EntityVisitor(World world, int i) {
		super(world, i);
	}

	public EntityVisitor(World world) {
		super(world);
	}

	public EntityVisitor(org.bukkit.World world) {
		this(world, null);
	}

	public EntityVisitor(org.bukkit.World world, VisitorGroup group) {
		super(((CraftWorld) world).getHandle());
		this.visitorGroup = group;
	}

	@Override
	protected void r() {
		this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityVillager.class, 3.0F));
		if (this.visitorGroup != null) {
			if (this.visitorGroup.getGuide() == this) {

			} else {
				this.goalSelector.a(1, new PathfinderGoalFollowGuide(this, 0.55, 4, 10000));
			}
		}
		this.goalSelector.a(9, new PathfinderGoalRandomStrollLand(this, 0.5D));
	}

}
