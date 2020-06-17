package ru.func.museum.visitor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author func 09.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class VisitorManager {

    private List<Visitor> visitors = new ArrayList<>();
    @NonNull
    private List<Location> locations;
    private int counter;
    private int size;

    public void spawn(Location location, int amount) {
        this.counter = 0;
        size = amount;
        for (int i = 0; i < size; i++)
            visitors.add(new Visitor(Excavation.WORLD.spawnEntity(location, EntityType.VILLAGER)));
    }

    public void clear() {
        counter = 0;
        visitors.clear();
        Excavation.WORLD.getEntities().stream()
                .filter(entity -> entity.getType() == EntityType.VILLAGER)
                .forEach(Entity::remove);
    }

    public Location getVictimFutureLocation() {
        counter = ++counter % size;
        val victim = visitors.get(counter);
        victim.visit(locations.get(Pickaxe.RANDOM.nextInt(locations.size()))
                .clone()
                .add(
                        Pickaxe.RANDOM.nextInt(10) - 5,
                        0,
                        Pickaxe.RANDOM.nextInt(10) - 5
                ));
        return victim.getEntity().getBukkitEntity().getLocation();
    }
}