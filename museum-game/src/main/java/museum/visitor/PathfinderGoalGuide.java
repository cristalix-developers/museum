package museum.visitor;

import net.minecraft.server.v1_12_R1.NavigationAbstract;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import org.bukkit.Location;

/**
 * @author func 16.09.2020
 * @project museum
 */
public class PathfinderGoalGuide extends PathfinderGoal {

	private final NavigationAbstract navigation;
	private final EntityVisitor guide;
	private final VisitorGroup group;
	private long idle;

	public PathfinderGoalGuide(VisitorGroup group) {
		this.group = group;
		this.guide = group.getGuide();
		this.navigation = guide.getNavigation();
	}

	// shouldExecute()
	@Override
	public boolean a() {
		return true;
	}

	@Override
	public boolean b() {
		return true;
	}

	// resetTask()
	@Override
	public void d() {
		// Ничего не делать при resetTask()
	}

	// updateTask()
	@Override
	public void e() {
		double distance = group.getCurrentNode().getLocation().distanceSquared(group.getGuide().getBukkitEntity().getLocation());
		if (distance < 4) {
			if (group.getCurrentNode().isImportant()) {
				if (idle == 0) idle = System.currentTimeMillis();
				if (System.currentTimeMillis() - idle < 5000) return;
				idle = 0;
			}
			if (group.getCurrentRoute() == null || group.getCurrentRoute().isEmpty()) {
				group.newMainRoute();
			}
			group.setCurrentNode(group.getCurrentRoute().poll());
			Location loc = group.getCurrentNode().getLocation();
			navigation.a(loc.x, loc.y, loc.z, 0.45);
		} else {
			Location loc = group.getCurrentNode().getLocation();
			navigation.a(loc.x, loc.y, loc.z, 0.45);
		}
	}
}