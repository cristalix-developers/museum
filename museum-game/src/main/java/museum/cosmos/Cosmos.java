package museum.cosmos;

import clepto.bukkit.B;
import clepto.bukkit.item.Items;
import clepto.bukkit.world.Label;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import me.func.mod.Anime;
import museum.App;
import museum.client_conversation.AnimationUtil;
import museum.cosmos.boer.Boer;
import museum.fragment.Fragment;
import museum.international.International;
import museum.player.User;
import museum.player.prepare.PreparePlayerBrain;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Stream;

public class Cosmos implements International {

    public static final Label ROCKET = App.getApp().getMap().getLabel("cosmos");
    public static final Label SPACE = App.getApp().getMap().getLabel("space");

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

        AnimationUtil.updateCosmoCrystal(user);

        player.setAllowFlight(true);
        player.setFlying(true);

        val inventory = player.getInventory();

        inventory.clear();

        player.getInventory().setArmorContents(armor);
        PreparePlayerBrain.givePickaxe(user);
        player.getInventory().setItem(8, BACK_ITEM);
        for (Fragment value : user.getRelics().values())
            if (value instanceof Boer)
                if (!((Boer) value).isStanding())
                    player.getInventory().addItem(value.getItem());

        Anime.topMessage(user.handle(), "Вы покинули землю 㕉");
        player.teleport(SPACE);
    }

    @Override
    public void leaveState(User user) {
        Anime.topMessage(user.handle(), "Вы вернулись на землю 㕉");
        AnimationUtil.leaveCosmos(user);
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
                user.giveCosmoCrystal(1, true);
                B.postpone(20 * 10, () -> block.setTypeAndDataFast(Material.STAINED_GLASS.id, (byte) 3));
            }
        }
    }
}
