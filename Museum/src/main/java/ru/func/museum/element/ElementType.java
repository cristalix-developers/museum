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
            1,
            2,
            "Кость левой ноги динозавра",
            100,
            SpaceType.DINOSAUR,
            ElementRare.USUAL
    ) {
        @Override
        public void show(PlayerConnection connection, Location location) {
            Vector3f vector = new Vector3f(
                    Pickaxe.RANDOM.nextFloat() * 360,
                    Pickaxe.RANDOM.nextFloat() * 360,
                    Pickaxe.RANDOM.nextFloat() * 360
            );
            int group = 1000 + Pickaxe.RANDOM.nextInt(9000);
            ElementType.sendSingle(connection, this, location.subtract(0, .6, 0), Material.QUARTZ_BLOCK, vector, group, 0);
            ElementType.sendSingle(connection, this, location.subtract(.2, -.4, .3), Material.QUARTZ_BLOCK, vector, group, 1);
        }
    },
    ;

    private int id;
    private int pieces;
    private String title;
    private double cost;
    private SpaceType spaceType;
    // Тип поля, на который можно поставить элемент
    private ElementRare elementRare;

    private static void sendSingle(PlayerConnection connection, ElementType element, Location location, Material material, Vector3f vector, int group, int subGroupId) {
        EntityArmorStand armorStand = new EntityArmorStand(Pickaxe.WORLD);
        armorStand.setCustomName(element.getTitle());
        armorStand.id = group * 100000 + (subGroupId + 10) * 1000 + element.getId();
        armorStand.setInvisible(true);
        armorStand.setCustomNameVisible(true);
        armorStand.setPosition(
                location.getX() + .5,
                location.getY() - 1,
                location.getZ() + .5
        );
        armorStand.setNoGravity(true);
        armorStand.setHeadPose(vector);
        connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStand));
        connection.sendPacket(new PacketPlayOutEntityEquipment(
                armorStand.getId(),
                EnumItemSlot.HEAD,
                CraftItemStack.asNMSCopy(new ItemStack(material))
        ));
    }

    public static ElementType findTypeById(int lastDigits) {
        for (ElementType type : ElementType.values())
            if (lastDigits == type.getId())
                return type;
        return null;
    }

    public abstract void show(PlayerConnection connection, Location location);
}
