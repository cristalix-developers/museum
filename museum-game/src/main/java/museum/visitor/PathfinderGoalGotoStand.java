package museum.visitor;

import lombok.val;
import net.minecraft.server.v1_12_R1.*;

/**
 * @author func 16.09.2020
 * @project museum
 */
public class PathfinderGoalGotoStand extends PathfinderGoal {

	private final NavigationAbstract navigation;
	private final VisitorGroup group;

	public PathfinderGoalGotoStand(VisitorGroup group) {
		this.group = group;
		this.navigation = group.getGuide().getNavigation();
	}

	@Override
	public void c() {
		val dotVisit = group.getCurrentRoute().get((int) (System.currentTimeMillis() / 100000) % group.getCurrentRoute().size()).getLocation();
		val guider = group.getGuide();
		navigation.a(guider.locX + (dotVisit.getX() - guider.locX) / 40, dotVisit.getY(), guider.locZ + (dotVisit.getZ() - guider.locZ) / 40, .65);
	}

	@Override
	public boolean a() {
		return true;
	}

	@Override
	public boolean b() {
		return false;
	}
}
