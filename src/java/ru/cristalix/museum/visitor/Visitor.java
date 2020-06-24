package ru.cristalix.museum.visitor;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;

/**
 * @author func 09.06.2020
 * @project Museum
 */
public class Visitor {

    @Getter
    private final EntityInsentient entity;

    public Visitor(org.bukkit.entity.Entity entity) {
        this.entity = (EntityInsentient) ((CraftEntity) entity).getHandle();
    }

    public void visit(Location meetingLocation) {
        entity.ticksLived = 0;
        entity.getNavigation().a(
                meetingLocation.getX(),
                meetingLocation.getY(),
                meetingLocation.getZ(),
                .8
        );
    }

}
