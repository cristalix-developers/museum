package ru.func.museum.element;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.func.museum.museum.space.SpaceType;
import ru.func.museum.player.pickaxe.Pickaxe;

@Getter
@AllArgsConstructor
public enum ElementType {
    BONE_DINOSAUR_LEG_LEFT(
            "Кость левой ноги динозавра",
            100,
            360,
            360,
            360,
            SpaceType.DINOSAUR,
            ElementRare.USUAL
    ) {
        @Override
        public void show(PlayerConnection connection, Location location) {
            Vector3f vector = new Vector3f(
                    Pickaxe.RANDOM.nextFloat() * getMaxXRotation(),
                    Pickaxe.RANDOM.nextFloat() * getMaxYRotation(),
                    Pickaxe.RANDOM.nextFloat() * getMaxZRotation()
            );
            ElementType.sendSingle(connection, location.subtract(0, .6, 0), getTitle(), Material.QUARTZ_BLOCK, vector);
            ElementType.sendSingle(connection, location.subtract(.2, -.4, .3), getTitle(), Material.QUARTZ_BLOCK, vector);
        }
    },
    ;

    private String title;
    private double cost;
    private float maxXRotation;
    private float maxYRotation;
    private float maxZRotation;
    private SpaceType spaceType;
    // Тип поля, на который можно поставить элемент
    private ElementRare elementRare;

    private static void sendSingle(PlayerConnection connection, Location location, String name, Material material, Vector3f vector) {
        EntityArmorStand armorStand = new EntityArmorStand(Pickaxe.WORLD);
        armorStand.setCustomName(name);
        armorStand.setInvisible(true);
        armorStand.setPosition(
                location.getX() + .5,
                location.getY() - 1,
                location.getZ() + .5
        );
        armorStand.setHeadPose(vector);
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.getId(),
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(new ItemStack(material))
        ));
    }

    public abstract void show(PlayerConnection connection, Location location);
}
