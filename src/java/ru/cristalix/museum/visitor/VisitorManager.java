package ru.cristalix.museum.visitor;

import clepto.cristalix.mapservice.Label;
import clepto.cristalix.mapservice.MapServiceException;
import org.bukkit.Location;
import ru.cristalix.museum.App;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorManager {

	private final List<? extends Location> node;
	private final List<VisitorGroup> groups;
	private final int visitorInGroup;
	private final int groupCount;
	private int wait = 0;
	private App app;

	public VisitorManager(App app, int groupCount, int visitorInGroup) {
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

	public void update(int counter) {
		if (wait > 0)
			wait--;
		if (groups.size() < groupCount && wait == 0) {
			wait = 30;
			groups.add(new VisitorGroup(app, node.get(0), visitorInGroup));
		}
		for (int i = 0; i < groups.size(); i++)
			groups.get(i).move(node.get((counter / 30 + i) % node.size()));
	}
}
