package ru.cristalix.museum.gui;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class MuseumItemHandler implements Listener {

	/*

    @NonNull
    private App app;
    private ItemStack museumItem;

    private final ItemStack museumChangeTitleItem = Lemonade.get("museum_change_title").render();
    private final ItemStack backItem = Lemonade.get("goback").render();
    private final ClickableItem alreadyHave = ClickableItem.empty(Lemonade.get("already_have").render());
    private final ItemStack gotoExcavationsItem = Lemonade.get("goto_excavations_item").render();
    private final ItemStack gotoPickaxesItem = Lemonade.get("goto_pickaxes_item").render();
    private final ItemStack collectorsItem = Lemonade.get("collectors").render();
    private final ItemStack inviteItem = Lemonade.get("invite").render();
    private final ItemStack buyCollectorItem = Lemonade.get("buy_collector").render();

//    private ControlledInventory excavationUI = ControlledInventory.builder()
//            .provider(new InventoryProvider() {
//                @Override
//                public void init(Player player, InventoryContents contents) {
//                    contents.resetMask("SSSSSSSSB");
//
//                    val user = app.getUser(player.getUniqueId());
//                    for (ExcavationType excavationType : ExcavationType.values()) {
//                        if (excavationType == ExcavationType.NOOP)
//                            continue;
//
//                        Excavation excavation = excavationType.getExcavation();
//                        if (excavation.getMinimalLevel() <= user.getLevel()) {
//                            contents.add('S', ClickableItem.of(
//                                    Lemonade.get("go").dynamic()
//                                            .fill("excavation", excavation.getTitle())
//                                            .fill("cost", String.format("%.2f", excavation.getCost()))
//                                            .fill("lvl", String.valueOf(excavation.getMinimalLevel()))
//                                            .fill("breaks", String.valueOf(excavation.getBreakCount()))
//                                            .render(),
//                                    event -> {
//                                        player.closeInventory();
//                                        if (excavation.getCost() > user.getMoney()) {
//                                            MessageUtil.find("nomoney").send(user);
//                                            return;
//                                        }
//                                        user.setMoney(user.getMoney() - excavation.getCost());
//                                        user.setBreakLess(excavation.getBreakCount());
//                                        user.setOnExcavation(true);
//
//                                        user.getCurrentMuseum().unload(user);
//                                        excavation.load(user, excavationType);
//                                    }
//                            ));
//                        }
//                    }
//                    contents.add('B', ClickableItem.of(backItem, event -> pickaxeUI.open(player)));
//                }
//            }).rows(1)
//            .title("Экспедиции")
//            .type(InventoryType.CHEST)
//            .build();

//    private ControlledInventory pickaxeBuyUI = ControlledInventory.builder()
//            .provider(new InventoryProvider() {
//                @Override
//                public void init(Player player, InventoryContents contents) {
//                    update(player, contents);
//                }
//
//                @Override
//                public void update(Player player, InventoryContents contents) {
//                    contents.resetMask("OOOOSOOOB");
//
//                    val user = app.getUser(player.getUniqueId());
//
//                    contents.add('B', ClickableItem.of(backItem, event -> pickaxeUI.open(player)));
//                    contents.fillMask('O', null);
//
//                    if (user.getPickaxeType() == PickaxeType.PRESTIGE) {
//                        contents.add('S', ClickableItem.empty(user.getPickaxeType().getItem()));
//                    } else {
//                        for (PickaxeType pickaxeType : PickaxeType.values()) {
//                            if (pickaxeType.getPrice() > user.getPickaxeType().getPrice()) {
//                                contents.add('S', ClickableItem.of(pickaxeType.getItem(), event -> {
//                                    if (user.getMoney() < pickaxeType.getPrice()) {
//                                        MessageUtil.find("nomoney").send(player);
//                                        player.closeInventory();
//                                        return;
//                                    }
//                                    contents.fillMask('S', null);
//                                    update(player, contents);
//                                    MessageUtil.find("newpickaxe").send(user);
//                                    user.setPickaxeType(pickaxeType);
//                                    user.setMoney(user.getMoney() - pickaxeType.getPrice());
//                                }));
//                                break;
//                            }
//                        }
//                    }
//                }
//            }).rows(1)
//            .title("Кирки")
//            .type(InventoryType.CHEST)
//            .build();

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
                    val user = app.getUser(player.getUniqueId());
                    val hall = user.getCurrentHall();
                    val museum = user.getCurrentMuseum();

                    contents.resetMask("SOSOSOOTB");

                    contents.add('B', ClickableItem.of(backItem, event -> museumUI.open(player)));
                    contents.add('T', ClickableItem.empty(collectorsItem));

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
                                            if (user.getMoney() < collectorType.getCost()) {
                                                MessageUtil.find("nomoney").send(user);
                                                player.closeInventory();
                                                return;
                                            }
                                            MessageUtil.find("newcollector")
                                                    .set("name", collectorType.getName())
                                                    .send(user);
                                            user.setMoney(user.getMoney() - collectorType.getCost());
                                            hall.setCollectorType(collectorType);

                                            // Перезагрузка музея
                                            museum.unload(user);
                                            museum.load(app, user);
                                            player.closeInventory();
                                        } else if (event.getClick().equals(ClickType.RIGHT)) {
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
                    val user = app.getUser(player.getUniqueId());
                    val museum = user.getCurrentMuseum();

                    contents.resetMask("OFOSMTOAO");

                    contents.add('F', ClickableItem.empty(
                            Lemonade.get("profile").dynamic()
                            .fill("level", String.valueOf(user.getLevel()))
                            .fill("money", MessageUtil.toMoneyFormat(user.getMoney()))
                            .fill("exp", String.valueOf(user.getExperience()))
                            .fill("need_exp", String.valueOf(user.getRequiredExperience(user.getLevel() + 1)))
                            .fill("hours_played", String.valueOf(player.getStatistic(Statistic.PLAY_ONE_TICK) / 720_000))
                            .fill("coins_picked", String.valueOf(user.getPickedCoinsCount()))
                            .fill("pickaxe", user.getPickaxeType().getName())
                            .fill("excavations", String.valueOf(user.getExcavationCount()))
                            .fill("fragments", String.valueOf(user.getElementList().size()))
                            .render()
                    ));
                    contents.add('M', ClickableItem.empty(getMuseumItem(user)));
                    contents.add('A', ClickableItem.of(inviteItem, event -> new VirtualSign().openSign(player, lines -> {
                                for (String line : lines) {
                                    if (line != null && !line.isEmpty()) {
                                        Player invited = Bukkit.getPlayer(line);
                                        if (invited != null) {
                                            if (invited.equals(player)) {
                                                MessageUtil.find("inviteyourself").send(user);
                                                return;
                                            }
                                            MessageUtil.find("invited").send(user);
                                            TextComponent invite = new TextComponent(
                                                    MessageUtil.find("invitefrom")
                                                            .set("player", player.getName())
                                                            .getText()
                                            );
                                            invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/museum accept " + player.getName()));
                                            invited.sendMessage(invite);
                                        } else
                                            MessageUtil.find("playeroffline").send(user);
                                        return;
                                    }
                                }
                            })
                    ));
                    contents.add('S', ClickableItem.of(buyCollectorItem, event -> collectorUI.open(player)
                    ));
                    contents.add('T', ClickableItem.of(museumChangeTitleItem,
                            event -> new VirtualSign().openSign(player, lines -> {
                                for (String line : lines) {
                                    if (line != null && !line.isEmpty()) {
                                        museum.setTitle(line);
                                        MessageUtil.find("museumtitlechange")
                                                .set("title", line)
                                                .send(user);
                                        return;
                                    }
                                }
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
        Player player = e.getPlayer();
        if (e.getItem() != null && e.getMaterial() != Material.AIR) {
            Material material = e.getMaterial();
            if (material == Material.PAPER) {
                museumUI.open(e.getPlayer());
            } else if (material == Material.EMERALD) {
                pickaxeUI.open(e.getPlayer());
            } else if (material == Material.SADDLE) {
                val user = app.getUser(player.getUniqueId());
                Museum museum = user.getCurrentMuseum();
                val owner = museum.getOwner();

                museum.unload(user);
                user.getMuseums().get("main").load(app, user);

                if (owner != null)
                    MessageUtil.find("leavedfrommuseum")
                            .set("name", player.getName())
                            .send(owner);

                MessageUtil.find("backtomuseum").send(user);
            }
        }
    }

    private ItemStack getMuseumItem(User user) {
        val museum = user.getCurrentMuseum();

        return Lemonade.get("museum").dynamic()
                .fill("owner", museum.getOwner().getName())
                .fill("title", museum.getTitle())
                .fill("views", String.valueOf(museum.getViews()))
                .fill("income", MessageUtil.toMoneyFormat(museum.getIncome()))
                .fill("collectors", String.valueOf(museum.getCollectorSlots()))
                .fill("spaces", String.valueOf(museum.getSubjects().size()))
                .fill("sinceCreation", LoveHumans.formatTime(System.currentTimeMillis() - museum.getCreationDate().getTime()))
                .render();

    }*/
}