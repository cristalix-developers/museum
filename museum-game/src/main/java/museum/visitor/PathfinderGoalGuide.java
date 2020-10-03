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
		B.bc("§7Called shouldExecute()");
		return true;
	}

	@Override
	public boolean b() {
		return true;
	}
	// resetTask()
	@Override
	public void d() {
		B.bc("§7Called resetTask()");
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
			B.bc("§eНовая цель:§f " + currentTarget);
		} else {
			Location loc = currentTarget.getLocation();
			navigation.a(loc.x, loc.y, loc.z, 0.65);
			B.bc("§7 " + loc);
			double distance = currentTarget.getLocation().distanceSquared(group.getGuide().getBukkitEntity().getLocation());
			B.bc("§7distance: " + Math.sqrt(distance));
			if (distance < 16) {
				B.bc("§eДостиг цели:§f " + currentTarget);
				idle = System.currentTimeMillis();
				group.setCurrentNode(currentTarget);
				currentTarget = null;
			}
		}
		B.bc("§7Called updateTask() " + currentTarget);

	}

}
