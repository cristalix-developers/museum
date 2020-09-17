package museum.visitor;

import clepto.cristalix.mapservice.MapServiceException;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.ticker.Ticked;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class VisitorHandler {

	@Getter
	private static final Map<UUID, VisitorGroup> visitorUuids = Maps.newHashMap();

	public static void init(App app, int groupAmount) {
		val labels = app.getMap().getLabels("node");
		val nodes = labels.stream()
				.filter(label -> label.getTag() != null)
				.collect(Collectors.toList());

		for (int i = 0; i < groupAmount; i++)
			new VisitorGroup(labels, nodes).spawn();

		if (labels.isEmpty())
			throw new MapServiceException("Not visitors nodes found.");
	}

}
