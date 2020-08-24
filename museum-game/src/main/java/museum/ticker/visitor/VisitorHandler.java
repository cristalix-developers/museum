package museum.ticker.visitor;

import clepto.cristalix.mapservice.Label;
import clepto.cristalix.mapservice.MapServiceException;
import museum.App;
import museum.ticker.Ticked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorHandler implements Ticked {

	public static final int VISITORS_IN_GROUP = 5;
	private final List<VisitorGroup> groups;

	public VisitorHandler() {
		groups = new ArrayList<>();

		List<Label> labels = App.getApp().getMap().getLabels("move");

		labels.forEach(node -> {
			String[] ss = node.getTag().split("\\s+");

			for (VisitorGroup visitorGroup : groups) {
				if (visitorGroup.getName().equals(ss[0])) {
					visitorGroup.getRoute().add(node);
					break;
				}
				groups.add(new VisitorGroup(ss[0]));
				groups.get(groups.size() - 1).getRoute().add(node);
			}
		});

		if (labels.isEmpty())
			throw new MapServiceException("Not visitors nodes found.");
	}

	@Override
	public void tick(int... args) {
		if (args[0] % 20 == 0)
			for (VisitorGroup group : groups)
				group.next();
	}
}
