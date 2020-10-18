package museum.visitor;

import clepto.ListUtils;
import clepto.bukkit.B;
import clepto.bukkit.world.Label;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ru.cristalix.core.formatting.Color;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.MAX_VALUE;

@Getter
public class VisitorGroup {

	private EntityVisitor guide;
	private List<EntityVisitor> crowd;
	private final Node[] nodes;
	private final List<Node> mainToVisit;
	private Deque<Node> currentRoute;
	@Setter
	private Node currentNode;
	@Setter
	private long idleStart;

	public VisitorGroup(List<Label> allNodes, List<Label> mainNodes) {
		int amount = allNodes.size();
		this.nodes = new Node[amount];
		int id = 0;
		int woolColor = 0;
		for (val loc : allNodes) {
			nodes[id] = new Node(id, loc.getTag(), new boolean[amount], loc);
			id++;
			loc.getChunk().load();
			Block block = loc.clone().add(0, -1, 0).getBlock();
			block.setType(Material.WOOL);
			block.setData((byte) woolColor++);
			if (woolColor == 16) woolColor = 0;
		}
		mainToVisit = new ArrayList<>();
		for (Node node : nodes) {
			if (mainNodes.contains(node.location))
				mainToVisit.add(node);

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
					if (new Location(from.getWorld(), dx * i, y, dz * i).getBlock().getType() != Material.AIR)
						failed = true;
				}
				if (failed) continue;

				int offset = (Integer.signum(dx) + Integer.signum(dz) + 1) / 2;
				if (dz != 0) offset += 2;

				if (distances[offset] > distance) continue;
				distances[offset] = distance;
				neighbours[offset] = adj;
			}

			for (val neighbour : neighbours) {
				if (neighbour == null) continue;
				neighbour.connections[node.id] = true;
				node.connections[neighbour.id] = true;
			}
		}
		this.currentRoute = new LinkedList<>(mainToVisit);
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
				if (v == target) {
					if (!visited.contains(v)) {
						visited.add(v);
						queue.add(v);
					}
				}
			}
		}
		B.bc("Новый маршрут: " + visited.stream().map(node -> {
			Color color = Color.values()[node.id % 16];
			return color.getChatFormat() + color.getTeamName();
		}).collect(Collectors.joining("§f, ")));
		this.currentRoute = new LinkedList<>(visited);
	}

	public void spawn() {
		nodes[0].getLocation().getChunk().load();
		currentNode = nodes[0];
		mainToVisit.remove(nodes[0]);
		Location loc = nodes[0].getLocation();
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

	@Data
	public static class Node {
		private final int id;
		private final String name;
		private final boolean[] connections;
		private final Location location;
	}

}
