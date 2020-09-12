package museum.visitor;

import clepto.cristalix.mapservice.MapServiceException;
import lombok.val;
import museum.App;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorHandler {

	public VisitorHandler() {
		val labels = App.getApp().getMap().getLabels("node");
		val nodes = labels.stream()
				.filter(label -> label.getTag() != null)
				.collect(Collectors.toList());

		val group = new VisitorGroup(labels, nodes);

		group.newMainRoute();
		group.spawn();

		if (labels.isEmpty())
			throw new MapServiceException("Not visitors nodes found.");
	}
}
