package ru.func.museum.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.cristalix.core.inventory.ClickableItem;
import ru.cristalix.core.inventory.ControlledInventory;
import ru.cristalix.core.inventory.InventoryContents;
import ru.cristalix.core.inventory.InventoryProvider;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.excavation.Excavation;
import ru.func.museum.excavation.ExcavationType;
import ru.func.museum.museum.AbstractMuseum;
import ru.func.museum.museum.collector.CollectorType;
import ru.func.museum.player.Archaeologist;
import ru.func.museum.player.pickaxe.PickaxeType;
import ru.func.museum.util.VirtualSign;

import java.text.DecimalFormat;
import java.util.*;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class MuseumItemHandler implements Listener {

    @NonNull
    private App app;
    private ItemStack museumItem;

    private static DecimalFormat numberFormat = new DecimalFormat("###,###,###,###,###,###.##$");

    private ItemStack backItem = Items.builder()
            .type(Material.BARRIER)
            .displayName("§cВернуться")
            .build();

    private ClickableItem alreadyHave = ClickableItem.empty(Items.builder()
            .displayName("§bПусто")
            .loreLines(
                    "",
                    "§fСдесь был коллектор,",
                    "§fкоторый вам не нужен,",
                    "§fпоэтому мы его убрали."
            ).type(Material.EMERALD)
            .build()
    );

    private ItemStack gotoExcavationsItem = Items.builder()
            .displayName("§bЭкспедиции")
            .type(Material.COMPASS)
            .lore("", "§fОтправтесь на раскопки", "§fи найдите следы прошлого.")
            .build();

    private ItemStack gotoPickaxesItem = Items.builder()
            .displayName("§bКирки")
            .type(Material.DIAMOND_PICKAXE)
            .lore("", "§fПриобретите новую кирку,", "§fи разгодайте тайны песка...")
            .build();

    private ControlledInventory excavationUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    contents.resetMask("SSSSSSSSB");

                    val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());
                    for (ExcavationType excavationType : ExcavationType.values()) {
                        if (excavationType == ExcavationType.NOOP)
                            continue;

                        Excavation excavation = excavationType.getExcavation();
                        if (excavation.getMinimalLevel() <= archaeologist.getLevel()) {
                            contents.add('S', ClickableItem.of(
                                    Items.builder()
                                            .displayName("§bВ путь!")
                                            .type(excavationType.getIcon())
                                            .lore(
                                                    "",
                                                    "§fЭкспедиция: " + excavation.getTitle(),
                                                    String.format("§fЦена: %.2f$", excavation.getCost()),
                                                    "§fМинимальный уровень: " + excavation.getMinimalLevel(),
                                                    "§fКол-во ударов: " + excavation.getBreakCount()
                                            ).build(),
                                    event -> {
                                        player.closeInventory();
                                        if (excavation.getCost() > archaeologist.getMoney()) {
                                            player.sendMessage("§7[§l§bi§7] У вас не достаточно средств. 㬏");
                                            return;
                                        }
                                        archaeologist.setMoney(archaeologist.getMoney() - excavation.getCost());
                                        archaeologist.setBreakLess(excavation.getBreakCount());
                                        archaeologist.setOnExcavation(true);
                                        archaeologist.setLastExcavation(excavationType);

                                        archaeologist.getCurrentMuseum().unload(app, archaeologist, player);
                                        excavation.load(archaeologist, player);
                                    }
                            ));
                        }
                    }
                    contents.add('B', ClickableItem.of(backItem, event -> pickaxeUI.open(player)));
                }
            }).rows(1)
            .title("Экспедиции")
            .type(InventoryType.CHEST)
            .build();

    private ControlledInventory pickaxeBuyUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    update(player, contents);
                }

                @Override
                public void update(Player player, InventoryContents contents) {
                    contents.resetMask("OOOOSOOOB");

                    val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());

                    contents.add('B', ClickableItem.of(backItem, event -> pickaxeUI.open(player)));
                    contents.fillMask('O', null);

                    if (archaeologist.getPickaxeType() == PickaxeType.PRESTIGE) {
                        contents.add('S', ClickableItem.empty(archaeologist.getPickaxeType().getItem()));
                    } else {
                        for (PickaxeType pickaxeType : PickaxeType.values()) {
                            if (pickaxeType.getPrice() > archaeologist.getPickaxeType().getPrice()) {
                                contents.add('S', ClickableItem.of(pickaxeType.getItem(), event -> {
                                    if (archaeologist.getMoney() < pickaxeType.getPrice()) {
                                        player.sendMessage("§7[§l§bi§7] У вас не достаточно средств. 㬏");
                                        player.closeInventory();
                                        return;
                                    }
                                    if (archaeologist.getPickaxeType().getPrice() >= pickaxeType.getPrice()) {
                                        player.sendMessage("§7[§l§bi§7] Эта кирка хуже вашей, вам не продадим! 㬏");
                                        player.closeInventory();
                                        return;
                                    }
                                    player.sendMessage("§7[§l§bi§7] Вы приобрели. Новое снаряжение!");
                                    archaeologist.setPickaxeType(pickaxeType);
                                    archaeologist.setMoney(archaeologist.getMoney() - pickaxeType.getPrice());
                                    update(player, contents);
                                }));
                                break;
                            }
                        }
                    }
                }
            }).rows(1)
            .title("Кирки")
            .type(InventoryType.CHEST)
            .build();

    private ControlledInventory pickaxeUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    contents.resetMask("OOSOOOSOO");
                    contents.add('S', ClickableItem.of(gotoExcavationsItem, event -> excavationUI.open(player)));
                    contents.add('S', ClickableItem.of(gotoPickaxesItem, event -> pickaxeBuyUI.open(player)));

                    contents.fillMask('O', null);
                }
            }).rows(1)
            .title("Раскопки и снаряжение")
            .type(InventoryType.CHEST)
            .build();

    private ControlledInventory collectorUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());
                    val hall = archaeologist.getCurrentHall();
                    val museum = archaeologist.getCurrentMuseum();

                    contents.resetMask("SOSOSOOTB");

                    contents.add('B', ClickableItem.of(backItem, event -> museumUI.open(player)));
                    contents.add('T', ClickableItem.empty(Items.builder()
                            .type(Material.PAPER)
                            .displayName("§bКоллекторы")
                            .lore("", "§fКупить за игрокую валюту - [ЛКМ],", "§fза кристалики - двойной клик [ЛКМ]")
                            .build()
                    ));

                    val collector = hall.getCollectorType();

                    for (CollectorType collectorType : CollectorType.values()) {
                        if (collectorType == CollectorType.NONE)
                            continue;
                        ClickableItem item = alreadyHave;
                        if (collector.getCost() <= collectorType.getCost()) {

                            List<String> lore = new ArrayList<>();

                            lore.add("");
                            if (collectorType.getSpeed() > 0)
                                lore.add("§fСкорость: " + collectorType.getSpeed() * 6);
                            if (collectorType.getRadius() > 0)
                                lore.add("§fРадиус сбора: " + collectorType.getRadius());
                            if (collectorType.getCost() > 0)
                                lore.add("§fИгровая цена: " + collectorType.getCost());
                            if (collectorType.getCristalixCost() > 0)
                                lore.add("§fКристаликов: §b" + collectorType.getCristalixCost());


                            item = ClickableItem.of(Items.builder()
                                            .type(collectorType.getHead().getType())
                                            .displayName("§b" +
                                                    collectorType.getName().toUpperCase().charAt(0) +
                                                    collectorType.getName().substring(1) +
                                                    (collector == collectorType ? " §l§f/ Выбрано" : "")
                                            ).lore(lore)
                                            .build(),
                                    event -> {
                                        if (collector == collectorType)
                                            return;
                                        if (event.getClick().equals(ClickType.LEFT)) {
                                            if (archaeologist.getMoney() < collectorType.getCost()) {
                                                player.sendMessage("§7[§l§bi§7] У вас не достаточно средств. 㬏");
                                                player.closeInventory();
                                                return;
                                            }
                                            player.sendMessage("§7[§l§bi§7] Вы приобрели " + collectorType.getName() + " коллектор!");
                                            archaeologist.setMoney(archaeologist.getMoney() - collectorType.getCost());
                                            hall.setCollectorType(collectorType);

                                            // Перезагрузка музея
                                            museum.unload(app, archaeologist, player);
                                            museum.load(app, archaeologist, player);
                                            player.closeInventory();
                                        } else if (event.getClick().equals(ClickType.DOUBLE_CLICK)) {
                                            player.sendMessage("Лол");
                                        }
                                    }
                            );
                        }
                        contents.add('S', item);
                    }
                    contents.fillMask('O', null);
                }
            }).title("Коллекторы")
            .rows(1)
            .type(InventoryType.CHEST)
            .build();

    private ControlledInventory museumUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                @Override
                public void init(Player player, InventoryContents contents) {
                    val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());
                    val museum = archaeologist.getCurrentMuseum();

                    contents.resetMask("OFOSMTOAO");

                    contents.add('F', ClickableItem.empty(Items.builder()
                            .type(Material.PAPER)
                            .displayName("§bПрофиль")
                            .loreLines(
                                    "",
                                    "§fУровень: " + archaeologist.getLevel(),
                                    String.format("§fДенег: %.2f$", archaeologist.getMoney()),
                                    "§fОпыт: " + archaeologist.getExp(),
                                    "§fОпыта осталось: " + archaeologist.expNeed(archaeologist.getExp()),
                                    "§fКирка: " + archaeologist.getPickaxeType().getName(),
                                    "§fРаскопок: " + archaeologist.getExcavationCount(),
                                    "§fФрагментов: " + archaeologist.getElementList().size(),
                                    "§fДрузей: " + archaeologist.getFriendList().size()
                            ).build()
                    ));
                    contents.add('M', ClickableItem.empty(getMuseumItem(archaeologist)));
                    contents.add('A', ClickableItem.of(Items.builder()
                                    .displayName("§bПригласить друга")
                                    .type(Material.BOOK_AND_QUILL)
                                    .lore("", "§fНажмите и введите", "§fникнейм приглашенного!")
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
                                    "§f[§bдля этого залла§f]"
                            ).build(), event -> collectorUI.open(player)
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
                    contents.fillMask('O', null);
                }
            }).title("Меню")
            .rows(1)
            .type(InventoryType.CHEST)
            .build();

    @EventHandler
    public void onEntityClick(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().getType() == EntityType.ARMOR_STAND)
            pickaxeUI.open(e.getPlayer());
    }

    @EventHandler
    public void onItemClick(PlayerInteractEvent e) {
        Player user = e.getPlayer();
        if (e.getItem() != null && e.getMaterial() != Material.AIR) {
            Material material = e.getMaterial();
            if (material == Material.PAPER) {
                museumUI.open(e.getPlayer());
            } else if (material == Material.EMERALD) {
                pickaxeUI.open(e.getPlayer());
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

    private ItemStack getMuseumItem(Archaeologist archaeologist) {
        if (museumItem == null) {
            museumItem = Items.builder()
                    .displayName("§bМузей")
                    .type(Material.CLAY_BALL)
                    .build();
            net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(museumItem);
            nmsItem.tag.setString("other", "guild_bank");
            museumItem.setItemMeta(CraftItemStack.getItemMeta(nmsItem));
        }
        val museum = archaeologist.getCurrentMuseum();
        val hall = archaeologist.getCurrentHall();

        ItemStack clone = museumItem.clone();
        ItemMeta meta = clone.getItemMeta();
        Date date = new Date();
        meta.setLore(Arrays.asList(
                "§fХозяин: " + museum.getOwner().getName(),
                "§fНазвание: " + museum.getTitle(),
                "§fЗаллов: " + museum.getHalls().size(),
                "§fПосещений: " + museum.getViews(),
                "",
                "§b > Залл",
                "§fДоход: " + numberFormat.format(museum.getSummaryIncrease()),
                "§fКоллектор: " + hall.getCollectorType().getName(),
                "§fВитрин: " + archaeologist.getCurrentHall().getMatrix().size(),
                "",
                "§7Создан " + (date.getTime() - museum.getDate().getTime()) / 3600_000 / 24 + " дней(я) назад"
        ));
        clone.setItemMeta(meta);
        return clone;
    }
}
