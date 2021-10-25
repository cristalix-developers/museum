package museum.cosmos;

import clepto.bukkit.B;
import clepto.bukkit.item.Items;
import clepto.bukkit.world.Label;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.international.International;
import museum.player.User;
import museum.player.prepare.PreparePlayerBrain;
import museum.util.StandHelper;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class Cosmos implements International {

    public static final Label ROCKET = App.getApp().getMap().getLabel("cosmos");
    public static final Label SPACE = App.getApp().getMap().getLabel("space");

    public static final ItemStack JETPACK = Items.render("jetpack").asBukkitMirror();

    private static final ItemStack[] armor = Stream.of(
                    "quantum-boots",
                    "quantum-leggings",
                    "quantum-chestplate",
                    "quantum-helmet"
            ).map(Items::render)
            .map(net.minecraft.server.v1_12_R1.ItemStack::asBukkitMirror)
            .toArray(ItemStack[]::new);

    public static final net.minecraft.server.v1_12_R1.ItemStack EARTH = Items.render("earth");

    @Getter
    @Setter
    public ArmorStand stand;

    @Override
    public void enterState(User user) {
        val player = user.getPlayer();

        player.setAllowFlight(false);
        player.setFlying(false);

        val inventory = player.getInventory();

        inventory.clear();

        player.getInventory().setArmorContents(armor);
        PreparePlayerBrain.givePickaxe(user);
        player.getInventory().addItem(JETPACK);

        AnimationUtil.topTitle(user, "Вы покинули землю 㕉");
        AnimationUtil.throwIconMessage(user, EARTH.asBukkitMirror(), "", "");

        player.teleport(SPACE);
    }

    @Override
    public void leaveState(User user) {
        AnimationUtil.leaveCosmos(user);
        AnimationUtil.topTitle(user, "Вы вернулись на землю 㕉");
        AnimationUtil.throwIconMessage(user, EARTH.asBukkitMirror(), "", "");
    }

    @Override
    public boolean playerVisible() {
        return true;
    }

    @Override
    public boolean nightVision() {
        return false;
    }

    @Override
    public void acceptBlockBreak(User user, PacketPlayInBlockDig packet) {
        if (packet.c == PacketPlayInBlockDig.EnumPlayerDigType.START_DESTROY_BLOCK) {
            val location = new Location(user.getWorld(), packet.a.getX(), packet.a.getY(), packet.a.getZ());
            val block = location.getBlock();

            if (block != null && block.getType() == Material.STAINED_GLASS) {
                block.setTypeAndDataFast(0, (byte) 0);
                user.giveCosmoCrystal(1);
                B.postpone(20 * 10, () -> block.setTypeAndDataFast(Material.STAINED_GLASS.id, (byte) 3));
            }
        }
    }

    @Override
    public boolean canBeBroken(BlockPosition pos) {
        return false;
    }

    public void useJetpack(Player player) {
        if (stand != null)
            return;
        stand = new StandHelper(player.getLocation().clone().add(0, 1, 0))
                .canMove(true)
                .passenger(player)
                .isInvisible(true)
                .isMarker(true)
                .hasGravity(false)
                .build();
    }


}
