package museum.ticker.visitor;

import clepto.cristalix.mapservice.Label;
import clepto.cristalix.mapservice.MapServiceException;
import org.bukkit.Location;
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

	private final List<? extends Location> node;
	private final List<VisitorGroup> groups;
	private final int visitorInGroup;
	private final int groupCount;
	private int wait = 0;
	private App app;

	public VisitorHandler(App app, int groupCount, int visitorInGroup) {
		this.app = app;
		groups = new ArrayList<>();

		List<Label> labels = app.getMap().getLabels("move");
		labels.sort(Comparator.comparingInt(Label::getTagInt));

		this.node = labels;
		this.groupCount = groupCount;
		this.visitorInGroup = visitorInGroup;

		if (labels.isEmpty())
			throw new MapServiceException("Not visitors nodes found.");
	}

	@Override
	public void tick(int... args) {
		if (wait > 0)
			wait--;
		if (groups.size() < groupCount && wait == 0) {
			wait = 30 * 20;
			groups.add(new VisitorGroup(app, node.get(0), visitorInGroup));
		}
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).move(node.get((args[0] / 30 + i) % node.size()));
	}
}
