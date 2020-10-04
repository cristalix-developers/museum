package museum.visitor;

import clepto.bukkit.B;
import net.minecraft.server.v1_12_R1.NavigationAbstract;
import net.minecraft.server.v1_12_R1.PathfinderGoal;
import org.bukkit.Location;

/**
 * @author func 16.09.2020
 * @project museum
 */
public class PathfinderGoalGuide extends PathfinderGoal {

	private final NavigationAbstract navigation;
	private final VisitorGroup group;
	private long idle;
	private VisitorGroup.Node currentTarget;

	public PathfinderGoalGuide(VisitorGroup group) {
		this.group = group;
		this.navigation = group.getGuide().getNavigation();
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

	}

	// updateTask()
	@Override
	public void e() {
		if (currentTarget == null) {
			if (System.currentTimeMillis() - idle < 5000) return;
			idle = 0;
			if (group.getCurrentRoute() == null || group.getCurrentRoute().isEmpty())
				group.newMainRoute();
			currentTarget = group.getCurrentRoute().poll();
			Location loc = currentTarget.getLocation();
			navigation.a(loc.x, loc.y, loc.z, 0.65);
		} else {
			Location loc = currentTarget.getLocation();
			navigation.a(loc.x, loc.y, loc.z, 0.65);
			double distance = currentTarget.getLocation().distanceSquared(group.getGuide().getBukkitEntity().getLocation());
			if (distance < 16) {
				idle = System.currentTimeMillis();
				group.setCurrentNode(currentTarget);
				currentTarget = null;
			}
		}
	}
}