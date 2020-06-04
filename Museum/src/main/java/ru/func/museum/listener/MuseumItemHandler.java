package ru.func.museum.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.cristalix.core.inventory.ClickableItem;
import ru.cristalix.core.inventory.ControlledInventory;
import ru.cristalix.core.inventory.InventoryContents;
import ru.cristalix.core.inventory.InventoryProvider;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.util.VirtualSign;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class MuseumItemHandler implements Listener {

    @NonNull
    private App app;
    private ItemStack museumItem;

    private ControlledInventory museumUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    Archaeologist archaeologist = App.getApp().getArchaeologistMap().get(player.getUniqueId());
                    AbstractMuseum museum = archaeologist.getCurrentMuseum();
                    contents.resetMask(
                            "XXXXXXXXX",
                            "OFOSMTOAO",
                            "XXXXXXXXX"
                    );
                    contents.add('F', ClickableItem.empty(Items.builder()
                            .type(Material.PAPER)
                            .displayName("§bПрофиль")
                            .loreLines(
                                    "",
                                    "§fУровень: " + archaeologist.getLevel(),
                                    String.format("§fДенег: %.2f$", archaeologist.getMoney()),
                                    "§fОпыт: " + archaeologist.getExp(),
                                    "§fОпыта осталось: " + archaeologist.expNeed(),
                                    "§fКирка: " + archaeologist.getPickaxeType().getName(),
                                    "§fРаскопок: " + archaeologist.getExcavationCount(),
                                    "§fФрагментов: " + archaeologist.getElementList().size(),
                                    "§fДрузей: " + archaeologist.getFriendList().size()
                            ).build()
                    ));
                    contents.add('M', ClickableItem.empty(getMuseumItem(museum)));
                    contents.add('A', ClickableItem.of(Items.builder()
                                    .displayName("§bПригласить друга")
                                    .type(Material.BOOK_AND_QUILL)
                                    .lore("", "§fНажмите ПКМ и введите", "§fникнейм приглашенного!")
                                    .build(), event -> new VirtualSign().openSign(player, lines -> {
                                        for (String line : lines) {
                                            if (line != null && !line.isEmpty()) {
                                                Player invited = Bukkit.getPlayer(line);
                                                if (invited != null) {
                                                    if (invited.equals(player)) {
                                                        player.sendMessage("§7[§l§bi§7] Вы так одиноки? 㬚");
                                                        return;
                                                    }
                                                    player.sendMessage("§7[§l§bi§7] Приглашение отправлено.");
                                                    TextComponent invite = new TextComponent("§7[§l§bi§7] Приглашение от " + player.getName() + ". [§6ПРИНЯТЬ§7]");
                                                    invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/museum accept " + player.getName()));
                                                    invited.sendMessage(invite);
                                                } else
                                                    player.sendMessage("§7[§l§bi§7] Игрок не в сети. 㬏");
                                                return;
                                            }
                                        }
                                        player.sendMessage("§7[§l§bi§7] Напишите хоть что-нибудь. 㬏");
                                    })
                    ));
                    contents.add('S', ClickableItem.of(Items.builder()
                                    .displayName("§bКупить коллектор")
                                    .type(Material.DISPENSER)
                                    .loreLines(
                                            "",
                                            "§fХороший коллектор способен",
                                            "§fсобрать больше монет!",
                                            "",
                                            "§f[§bДЛЯ ЭТОГО МУЗЕЯ§f]"
                                    ).build(), event -> {
                            }
                    ));
                    contents.add('T', ClickableItem.of(Items.builder()
                                    .displayName("§bПереименовать музей")
                                    .type(Material.SIGN)
                                    .loreLines(
                                            "",
                                            "§fЕсли вам не нравтся",
                                            "§fназвание вашего музея",
                                            "§fвы можете его изменить."
                                    ).build(),
                            event -> new VirtualSign().openSign(player, lines -> {
                                for (String line : lines) {
                                    if (line != null && !line.isEmpty()) {
                                        museum.setTitle(line);
                                        player.sendMessage("§7[§l§bi§7] Название музея изменено на \"" + line + "\".");
                                        return;
                                    }
                                }
                                player.sendMessage("§7[§l§bi§7] Вы написали пустую строку. Так музей не называют. 㬏");
                            })
                    ));
                    contents.fillMask('X', ClickableItem.empty(Items.builder()
                            .displayName("§7пустота")
                            .type(Material.STAINED_GLASS_PANE)
                            .damage((short) 0)
                            .build()
                    ));
                    contents.fillMask('O', null);
                }
            }).title("Меню")
            .rows(3)
            .type(InventoryType.CHEST)
            .build();

    private ItemStack getMuseumItem(AbstractMuseum museum) {
        if (museumItem == null) {
            museumItem = Items.builder()
                    .displayName("§bНастройки музея")
                    .type(Material.CLAY_BALL)
                    .build();
            net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(museumItem);
            nmsItem.tag.setString("other", "guild_bank");
            museumItem.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
        }
        ItemStack clone = museumItem.clone();
        ItemMeta meta = clone.getItemMeta();
        Date date = new Date();
        meta.setLore(Arrays.asList(
                "",
                "§fНазвание: " + museum.getTitle(),
                "§fДоход: ",
                "§fПосещений: " + museum.getViews(),
                "§fКоллектор: " + museum.getCollectorType().getName(),
                "§fВитрин: " + museum.getMatrix().size(),
                "",
                "§7Создан " + (date.getTime() - museum.getDate().getTime())/3600_000/24 + " дней(я) назад"
        ));
        clone.setItemMeta(meta);
        return clone;
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        Player user = e.getPlayer();
        if (e.getItem() != null && e.getMaterial() != Material.AIR) {
            Material material = e.getMaterial();
            if (material == Material.PAPER) {
                museumUI.open(e.getPlayer());
            } else if (material.name().contains("PICKAXE")) {

            } else if (material == Material.SADDLE) {
                val player = app.getArchaeologistMap().get(user.getUniqueId());
                AbstractMuseum museum = player.getCurrentMuseum();
                val ownerArchaeologist = museum.getOwner();

                if (ownerArchaeologist.equals(player)) {
                    user.sendMessage("§7[§l§bi§7] §7Вы уже в своем музее!");
                    user.getInventory().remove(Material.SADDLE);
                    return;
                }

                museum.unload(app, ownerArchaeologist, user);
                player.getMuseumList().get(0).load(app, player, user);
                val owner = Bukkit.getPlayer(UUID.fromString(ownerArchaeologist.getUuid()));

                if (owner != null)
                    owner.sendMessage("§7[§l§bi§7] §7" + user.getName() + " покинул ваш музей.");

                user.sendMessage("§7[§l§bi§7] §7Вы вернулись в свой музей.");
            }
        }
    }
}
