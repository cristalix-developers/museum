package museum.visitor;

import clepto.ListUtils;
import clepto.bukkit.world.Label;
import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

@Getter
public class VisitorGroup {

	private EntityVisitor guide;
	private List<EntityVisitor> crowd;
	private final List<Node> nodes = new ArrayList<>();
	private final List<Node> mainToVisit = new ArrayList<>();
	private Deque<Node> currentRoute;
	@Setter
	private Node currentNode;
	@Setter
	private long idleStart;

	public VisitorGroup(List<Label> allNodes, List<Label> mainNodes) {
		int woolColor = 0;

		List<Node> nodes = new ArrayList<>();
		for (Label nodeLabel : allNodes) {
			nodes.add(new Node(nodeLabel.getTag(), nodeLabel));
			nodeLabel.getChunk().load();
			Block block = nodeLabel.clone().add(0, -1, 0).getBlock();
			block.setType(Material.WOOL);
			block.setData((byte) woolColor++);
			if (woolColor == 16) woolColor = 0;
		}

		for (Node node : nodes) {
			if (node.isImportant()) mainToVisit.add(node);

			for (Node another : nodes) {
				if (another == node) continue;
				if (another.getLocation().distanceSquared(node.getLocation()) < 121) {
					node.getNeighbours().add(another);
					another.getNeighbours().add(node);
				}
			}
		}

		currentNode = nodes.get(0);

	}

	public List<Node> route(Node sourceNode, Node destinationNode) {

		Map<Node, Node> previousNodeMap = new HashMap<>();
		Node currentNode = sourceNode;

		Queue<Node> queue = new LinkedList<>();
		queue.add(currentNode);

		Set<Node> visitedNodes = new HashSet<>();
		visitedNodes.add(currentNode);

		// Search
		while (!queue.isEmpty()) {
			currentNode = queue.remove();
			if (currentNode.equals(destinationNode)) break;

			for (Node nextNode : currentNode.getNeighbours()) {
				if (visitedNodes.contains(nextNode)) continue;

				queue.add(nextNode);
				visitedNodes.add(nextNode);

				// Look up of next node instead of previous.
				previousNodeMap.put(nextNode, currentNode);
			}
		}

		// If all nodes are explored and the destination node hasn't been found.
		if (!currentNode.equals(destinationNode))
			throw new RuntimeException("No feasible path.");

		// Reconstruct path from the tail
		List<Node> directions = new LinkedList<>();
		for (Node node = destinationNode; node != null; node = previousNodeMap.get(node)) {
			directions.add(node);
		}

		Collections.reverse(directions);

		return directions;

	}

	public void newMainRoute() {
		Node target = ListUtils.random(this.mainToVisit);
		this.mainToVisit.remove(target);
		this.currentRoute = new LinkedList<>(route(this.currentNode, target));
	}

	public void spawn() {
		Location loc = currentNode.getLocation();
		loc.getChunk().load();
		mainToVisit.remove(currentNode);
		this.guide = new EntityVisitor(loc.getWorld(), this);
		this.guide.setCustomName("Гид");
		this.guide.setCustomNameVisible(true);
		PatchedEntity.VISITOR.spawn(this.guide, loc);
		int peopleAmount = 5 + ((int) (Math.random() * 6));
		this.crowd = new ArrayList<>();
		for (int i = 0; i < peopleAmount; i++) {
			val visitor = new EntityVisitor(loc.getWorld(), this);
			this.crowd.add(visitor);
			PatchedEntity.VISITOR.spawn(visitor, loc);
		}
	}

	@Getter
	@RequiredArgsConstructor
	@ToString
	public static class Node {

		private final String name;
		private final Location location;

		@ToString.Exclude
		private final List<Node> neighbours = new ArrayList<>();

		public boolean isImportant() {
			return name != null && !name.isEmpty();
		}

	}

}
