package museum.entities;

import clepto.ListUtils;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;

import static java.lang.Integer.MAX_VALUE;

@Getter
public class VisitorGroup {

	private EntityVisitor guide;
	private List<EntityVisitor> crowd;
	private final Node[] nodes;
	private final List<Node> mainToVisit;
	private List<Node> currentRoute;
	private Node currentNode;

	public VisitorGroup(List<Location> allNodes, List<Location> mainNodes) {
		int amount = allNodes.size();
		this.nodes = new Node[amount];
		int id = 0;
		for (Location loc : allNodes) {
			nodes[id] = new Node(id, new boolean[amount], loc);
			id++;
		}
		mainToVisit = new ArrayList<>();
		for (Node node : nodes) {
			if (mainNodes.contains(node.location)) mainToVisit.add(node);

			int[] distances = {MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE};
			Node[] neighbours = {null, null, null, null};
			for (Node adj : nodes) {
				if (node == adj) continue;
				int dx = adj.location.getBlockX() - node.location.getBlockX();
				int dz = adj.location.getBlockZ() - node.location.getBlockZ();
				if (dx != 0 && dz != 0) continue; // Only 90-degree nodes are considered to be adjacent.
				int y = Math.max(adj.location.getBlockY(), node.location.getBlockY());
				Location from = (dx + dz > 0 ? node : adj).location;
				boolean failed = false;
				int distance = Math.abs(dx + dz);
				for (int i = 0; i < distance; i++) {
					if (new Location(from.getWorld(), dx * i, y, dz * i).getBlock().getType() != Material.AIR) failed = true;
				}
				if (failed) continue;

				int offset = (Integer.signum(dx) + Integer.signum(dz) + 1) / 2;
				if (dz != 0) offset += 2;

				if (distances[offset] > distance) continue;
				distances[offset] = distance;
				neighbours[offset] = adj;
			}

			for (Node neighbour : neighbours) {
				if (neighbour == null) continue;
				neighbour.connections[node.id] = true;
				node.connections[neighbour.id] = true;
			}

		}

	}

	public void newMainRoute() {
		Node target = ListUtils.random(mainToVisit);
		mainToVisit.remove(target);

		Set<Node> visited = new LinkedHashSet<>();
		Queue<Node> queue = new LinkedList<>();
		queue.add(currentNode);
		visited.add(currentNode);

		while (!queue.isEmpty()) {
			Node vertex = queue.poll();
			for (int i = 0; i < vertex.connections.length; i++) {
				if (i == vertex.id || !vertex.connections[i]) continue;
				Node v = nodes[i];
				if (v == target)
				if (!visited.contains(v)) {
					visited.add(v);
					queue.add(v);
				}
			}
		}

		this.currentRoute = new ArrayList<>(visited);

	}

	public void spawn(Node node) {
		currentNode = node;
		mainToVisit.remove(node);
		Location loc = node.getLocation();
		this.guide = new EntityVisitor(loc.getWorld(), this);
		PatchedEntity.VISITOR.spawn(this.guide, loc);
		int peopleAmount = 5 + ((int) (Math.random() * 6));
		this.crowd = new ArrayList<>();
		for (int i = 0; i < peopleAmount; i++) {
			EntityVisitor visitor = new EntityVisitor(loc.getWorld(), this);
			PatchedEntity.VISITOR.spawn(visitor, loc);
			this.crowd.add(visitor);
		}
	}

	@Data
	public static class Node {
		private final int id;
		private final boolean[] connections;
		private final Location location;
	}

}
