package museum.visitor;

import lombok.val;
import museum.App;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.UUID;

public class PathfinderGoalFollowGuide extends PathfinderGoal {

	private final EntityVisitor visitor;
	private EntityLiving guide;
	World world;
	private final double followSpeed;
	private final NavigationAbstract navigation;
	private int h;
	float maxDist;
	float minDist;

	public PathfinderGoalFollowGuide(EntityVisitor visitor, double followSpeed, float minDistance, float maxDistance) {
		this.visitor = visitor;
		this.world = visitor.world;
		this.followSpeed = followSpeed;
		this.navigation = visitor.getNavigation();
		this.minDist = minDistance;
		this.maxDist = maxDistance;
		this.a(3);
		if (!(visitor.getNavigation() instanceof Navigation) && !(visitor.getNavigation() instanceof NavigationFlying)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	// shouldExecute()
	@Override
	public boolean a() {
		val uuids = VisitorHandler.getVisitorUuids();

		if (!uuids.containsKey(visitor.getUniqueID())) {
			return false;
		}
		val visitorGroup = uuids.get(visitor.getUniqueID());
		if (visitorGroup == null) return false;
		EntityVisitor guide = visitorGroup.getGuide();
		if (guide == null) return false;
		if (this.visitor.h(guide) < (double) (this.minDist * this.minDist)) return false;
		this.guide = guide;
		return true;
	}

	// shouldContinueExecuting()
	@Override
	public boolean b() {
		return !this.navigation.o() && this.visitor.h(this.guide) > (double) (this.maxDist * this.maxDist);
	}

	// startExecuting()
	@Override
	public void c() {
		this.h = 0;
	}

	// resetTask()
	@Override
	public void d() {
		this.guide = null;
	}

	// updateTask()
	@Override
	public void e() {
		this.visitor.getControllerLook().a(this.guide, 10.0F, (float) this.visitor.N());
		if (--this.h > 0) return;
		this.h = 10;

		// tryMoveToEntityLiving(...)
		if (this.navigation.a(this.guide, this.followSpeed)) return;

		if (this.visitor.isLeashed()) return;
		if (this.visitor.isPassenger()) return;

		// getDistanceSqToEntity(...)
		if (this.visitor.h(this.guide) < 144.0D) return;

		int i = MathHelper.floor(this.guide.locX) - 2;
		int j = MathHelper.floor(this.guide.locZ) - 2;
		int k = MathHelper.floor(this.guide.getBoundingBox().b);

		for (int l = 0; l <= 4; ++l) {
			for (int i1 = 0; i1 <= 4; ++i1) {
				if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportationViable(i, j, k, l, i1)) {
					CraftEntity entity = this.visitor.getBukkitEntity();
					Location to = new Location(entity.getWorld(), (float) (i + l) + 0.5F, k, (float) (j + i1) + 0.5F, this.visitor.yaw, this.visitor.pitch);
					EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
					this.visitor.world.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) return;

					to = event.getTo();
					this.visitor.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
					this.navigation.p();
					return;
				}
			}
		}

	}

	protected boolean isTeleportationViable(int i, int j, int k, int l, int i1) {
		BlockPosition blockposition = new BlockPosition(i + l, k - 1, j + i1);
		IBlockData iblockdata = this.world.getType(blockposition);
		return iblockdata.d(this.world, blockposition, EnumDirection.DOWN) == EnumBlockFaceShape.SOLID && iblockdata.a(this.visitor) && this.world.isEmpty(blockposition.up()) && this.world.isEmpty(
				blockposition.up(2));
	}

}
