package museum.util;

import museum.App;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class StandHelper {

    private final ArmorStand stand;
    private final EntityArmorStand craftArmorStand;

    public StandHelper(Location location) {
        this.stand = location.world.spawn(location, ArmorStand.class);
        this.craftArmorStand = ((CraftArmorStand) this.stand).getHandle();
    }

    public StandHelper removeWhenFarAway(boolean b) {
        stand.setRemoveWhenFarAway(b);
        return this;
    }

    public StandHelper health(double health) {
        stand.setMaxHealth(health);
        stand.setHealth(health);
        return this;
    }

    public StandHelper customName(String name) {
        stand.setCustomName(name);
        stand.setCustomNameVisible(true);
        return this;
    }

    public StandHelper hasGravity(boolean b) {
        craftArmorStand.setNoGravity(!b);
        return this;
    }

    public StandHelper hasArms(boolean b) {
        stand.setArms(b);
        return this;
    }

    public StandHelper hasBasePlate(boolean b) {
        stand.setBasePlate(b);
        return this;
    }

    public StandHelper isMarker(boolean b) {
        stand.setMarker(b);
        return this;
    }

    public StandHelper isInvisible(boolean b) {
        craftArmorStand.setInvisible(b);
        return this;
    }

    public StandHelper isSmall(boolean b) {
        stand.setSmall(b);
        return this;
    }

    public StandHelper isInvulnerable(boolean b) {
        stand.setInvulnerable(b);
        return this;
    }

    public StandHelper slot(EnumItemSlot slot, ItemStack itemStack) {
        craftArmorStand.setSlot(slot, itemStack);
        return this;
    }

    public StandHelper headPose(double x, double y, double z) {
        stand.setHeadPose(stand.getHeadPose().add(x, y, z));
        return this;
    }

    public StandHelper fixedData(String key, Object value) {
        stand.setMetadata(key, new FixedMetadataValue(App.getApp(), value));
        return this;
    }

    public StandHelper passenger(Entity entity) {
        stand.setPassenger(entity);
        return this;
    }

    public StandHelper canMove(boolean b) {
        stand.setCanMove(b);
        return this;
    }

    public ArmorStand build() {
        return stand;
    }
}
