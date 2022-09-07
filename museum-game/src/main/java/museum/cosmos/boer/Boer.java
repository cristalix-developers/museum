package museum.cosmos.boer;

import clepto.bukkit.item.Items;
import implario.humanize.Humanize;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import museum.fragment.Fragment;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.player.User;
import museum.util.StandHelper;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Material.COBBLE_WALL;

@Getter
public class Boer implements Fragment {

    private static final net.minecraft.server.v1_12_R1.ItemStack WALL = CraftItemStack.asNMSCopy(new ItemStack(COBBLE_WALL));
    private static final net.minecraft.server.v1_12_R1.ItemStack PRISMARINE = CraftItemStack.asNMSCopy(new ItemStack(Material.PRISMARINE));
    private static final net.minecraft.server.v1_12_R1.ItemStack ANVIL = CraftItemStack.asNMSCopy(new ItemStack(Material.ANVIL));
    private static final net.minecraft.server.v1_12_R1.ItemStack ANTENNA = Items.render("antenna");

    @Setter
    private BoerType type;
    private final UUID uuid;
    private final ItemStack item;
    private final UUID owner;
    @Setter
    private int secondsLeft;
    private List<ArmorStand> stands;
    private ArmorStand head;
    @Setter
    private boolean notification = true;

    public Boer(String data, UUID owner) {
        this.type = BoerType.valueOf(data.replace("boer_", ""));
        this.uuid = UUID.randomUUID();
        this.owner = owner;

        ItemStack item = new ItemStack(Material.CLAY_BALL);
        val nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.tag = new NBTTagCompound();
        nmsItem.tag.setString("boer-uuid", uuid.toString());
        nmsItem.tag.setString("other", "win2");

        this.item = nmsItem.asBukkitMirror();

        val meta = this.item.getItemMeta();

        meta.setDisplayName("§bКосмический бур §l§f" + (1 + type.ordinal()) + "§fУР.");
        meta.setLore(Arrays.asList(
                "",
                "§7Данный бур работает",
                "§l§f" + getType().getTime() / 3600 + " §7" + Humanize.plurals("час", "часа", "часов", type.getTime()) + " и приносит",
                "§b1 опыт §7и §b1 кристалл",
                "§7каждые §l§f" + type.getSpeed() + " §7" + Humanize.plurals("секунда", "секунды", "секунд", type.getSpeed())
        ));

        this.item.setItemMeta(meta);
    }

    @Override
    public String getAddress() {
        return "boer_" + type.name();
    }

    @Override
    public int getPrice() {
        return 0;
    }

    public void view(User user, Location location) {
        if (isStanding()) {
            MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.error("У вас уже стоит этот бур!"));
            return;
        }
        val player = user.getPlayer();
        val item = player.inventory.getItemInHand();
        val nmsItem = CraftItemStack.asNMSCopy(item);

        val itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(type.getAddress() + " Бур");
        item.setItemMeta(itemMeta);

        if (nmsItem.getTag() == null || !nmsItem.getTag().hasKeyOfType("boer-uuid", 8))
            return;

        val fragment = (Boer) user.getRelics().get(UUID.fromString(nmsItem.getTag().getString("boer-uuid")));
        val boerTag = uuid.toString();
        val boerOwnerTag = owner.toString();

        head = new StandHelper(location.clone().add(0.0D, 0.5D, 0.1D))
                .isInvisible(true)
                .hasGravity(false)
                .slot(EnumItemSlot.HEAD, ANTENNA)
                .fixedData("boer", boerTag)
                .fixedData("owner", boerOwnerTag)
                .customName("§aАктивация бура...")
                .build();

        player.setItemInHand(null);
        stands = new ArrayList<>(Arrays.asList(
                head,
                new StandHelper(location)
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, Items.render("relic-tooth"))
                        .headPose(Math.PI / 7, Math.PI, 0.0D)
                        .isSmall(true)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build(),
                new StandHelper(location.clone().add(0.0D, 0.0D, 0.2D))
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, Items.render("relic-tooth"))
                        .headPose(Math.PI / 7, -0.0D, 0.0D)
                        .isSmall(true)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build(),
                new StandHelper(location.clone().add(0.0D, -0.5D, 0.1D))
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, WALL)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build(),
                new StandHelper(location.clone().add(0.0D, -0.0D, 0.1D))
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, PRISMARINE)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build(),
                new StandHelper(location.clone().add(-0.1D, 0.8D, 0.1D))
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, fragment.type.getBlock())
                        .isSmall(true)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build(),
                new StandHelper(location.clone().add(0.0D, 1.3D, 0.1D))
                        .isInvisible(true)
                        .hasGravity(false)
                        .slot(EnumItemSlot.HEAD, ANVIL)
                        .isSmall(true)
                        .fixedData("boer", boerTag)
                        .fixedData("owner", boerOwnerTag)
                        .build()
        ));

        secondsLeft = type.getTime();

        BoerManager.createActiveBoer(this);
    }

    public Boer boerRemove() {
        val player = Bukkit.getPlayer(this.owner);
        getStands().forEach(armorStand -> {
            ((CraftArmorStand) armorStand).getHandle().killEntity();
            armorStand.remove();
        });
        if (getStands() != null)
            getStands().clear();
        if (player != null && player.isOnline()) {
            player.getInventory().addItem(item);
            MultiChatUtil.sendMessage(player, ChatType.SYSTEM, Formatting.fine("Ваш бур прекратил работу."));
        }
        return this;
    }

    public boolean isStanding() {
        return stands != null && !stands.isEmpty();
    }
}
