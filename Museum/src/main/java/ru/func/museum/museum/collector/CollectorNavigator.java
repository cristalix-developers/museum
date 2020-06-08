package ru.func.museum.museum.collector;


import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 07.06.2020
 * @project Museum
 */
@Getter
public class CollectorNavigator {

    private final World world;
    private final List<Node> nodes;
    private double totalLocation;

    public CollectorNavigator(World world, List<Location> points) {
        this.world = world;
        this.nodes = points.stream().map(Node::new).collect(Collectors.toList());
        for (int i = 0; i < this.nodes.size(); i++) {
            Node a = nodes.get(i);
            Node b = nodes.get(i == 0 ? this.nodes.size() - 1 : i - 1);
            b.next = a;
            totalLocation += b.distanceToNext = b.location.distance(a.location);
        }

        for (Node node : nodes)
            node.part = node.distanceToNext / totalLocation;
    }

    public Location getLocation(double part) {
        part -= Math.floor(part);
        double s = totalLocation * part;
        double p = 0;
        Node node = null;
        for (Node n : nodes) {
            s -= n.distanceToNext;
            p += n.part;
            if (s > 0)
                continue;
            node = n;
            break;
        }
        if (node == null)
            return null;
        Location from = node.location;
        Location to = node.next.location;
        float yaw = (float) Math.toDegrees(Math.atan2(to.getX() - from.getX(), from.getZ() - to.getZ()));
        //float pitch = (float) Math.toDegrees(Math.atan2(to.getY() - from.getY(), Math.sqrt(NumberConversions.square(to.getX() - from.getX()) + NumberConversions.square(to.getZ() - from.getZ()))));
        double localPart = (part - p) / node.part;
        double x = (to.getX() - from.getX()) * localPart;
        double y = (to.getY() - from.getY()) * localPart;
        double z = (to.getZ() - from.getZ()) * localPart;
        return new Location(world, x + to.getX(), y + to.getY(), z + to.getZ(), yaw, 0); // pitch
    }

    @Data
    private static class Node {
        private final Location location;
        private double distanceToNext;
        private double part;
        @ToString.Exclude
        private Node next;
    }
}
