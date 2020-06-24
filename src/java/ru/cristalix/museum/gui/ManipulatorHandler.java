package ru.cristalix.museum.gui;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;

/**
 * @author func 05.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class ManipulatorHandler implements Listener {

    // todo: remake
/*
    @NonNull
    private App app;

    private ControlledInventory spaceUiTemplate = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                          @Override
                          public void init(Player player, InventoryContents contents) {
                              contents.resetMask(
                                      "OXXXXXXXX",
                                      "XXXXXXXXX",
                                      "XXXXXXXXX",
                                      "XXXXXXXXX",
                                      "XXXXXXXXX",
                                      "XXXXXXXXX"
                              );

                              val user = app.getUser(player.getUniqueId());
                              val museum = user.getCurrentMuseum();
                              val space = user.getCurrentSubject();

                              contents.add('O', ClickableItem.of(clear, event -> {
                                  for (Element element : user.getElementList())
                                      for (Element spaceElement : space.getElements())
                                          if (element.getId() == spaceElement.getId() && element.getParentId() == spaceElement.getParentId())
                                              element.setLocked(false);

                                  space.hide(user);
                                  player.closeInventory();
                                  MessageUtil.find("freestand").send(user);
                                  space.getElements().clear();
                                  museum.updateIncrease();
                              }));

                              for (int i = 0; i < app.getMuseumEntities().length; i++) {
                                  // Проверка на то, является ли динозавр разрешенным
                                  boolean forbidden = true;

                                  for (int able : space.getAccessEntities()) {
                                      if (able == i) {
                                          forbidden = false;
                                          break;
                                      }
                                  }

                                  if (forbidden)
                                      continue;

                                  val entity = app.getMuseumEntities()[i];
                                  val parentId = i;

                                  List<Element> elements = user.getElementList().stream()
                                          .filter(element -> element.getParentId() == parentId)
                                          .collect(Collectors.toList());

                                  Items.Builder item = Items.builder()
                                          .type(entity.getSubs()[0].getPieces().get(0).getMaterial());

                                  ClickableItem clickableItem;

                                  if (elements.size() == 0) {
                                      clickableItem = ClickableItem.empty(item
                                              .displayName("§b" + entity.getTitle() + "§f / Не изучен")
                                              .build()
                                      );
                                  } else {
                                      String[] fragments = new String[entity.getSubs().length + 1];

                                      fragments[0] = "";

                                      for (int j = 0; j < fragments.length - 1; j++) {
                                          boolean contains = false;

                                          for (Element element : elements) {
                                              if (element.getId() == j) {
                                                  contains = true;
                                                  break;
                                              }
                                          }
                                          fragments[j + 1] = "§" + (contains ? "a + " : "c - ") + "§f" +
                                                  entity.getSubs()[j].getTitle();
                                      }

                                      clickableItem = ClickableItem.of(item.displayName("" +
                                                      "§b" + entity.getTitle() +
                                                      "§f " + elements.size() +
                                                      "/" + (fragments.length - 1) +
                                                      " фрагментов"
                                              ).lore(fragments).build(), event -> {
                                                  if (elements.get(0).isLocked()) {
                                                      MessageUtil.find("standlocked").send(user);
                                                      player.closeInventory();
                                                      return;
                                                  }
                                                  for (Element element : elements) {
                                                      element.setLocked(true);
                                                      space.getElements().add(element);
                                                  }
                                                  // Перезапуск витрины
                                                  museum.updateIncrease();
                                                  space.hide(user);
                                                  space.show(user);
                                                  player.closeInventory();
                                                  MessageUtil.find("standplaced").send(user);
                                              }
                                      );
                                  }
                                  contents.add('X', clickableItem);
                              }
                          }
                      }
            ).rows(6)
            .title("Выбор экспоната")
            .build();

    private ItemStack clear = Items.builder()
            .displayName("§cОсвободить витрину")
            .lore("", "§7Ваши экспонаты НЕ исчезнут!", "§7Вы их сможете поставить", "§7на другую витрину.")
            .type(Material.BARRIER)
            .build();

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Material type = e.getClickedBlock().getType();
			if (type != Material.SIGN_POST && type != Material.WALL_SIGN) return;

            val user = app.getUser(e.getPlayer().getUniqueId());
			for (MuseumPrototype prototype : app.getMuseumManager().getMuseumPrototypeMap().values()) {
			}
            user.getCurrentMuseum().getSubjects().stream()
                    .filter(space -> space.isManipulator(e.getClickedBlock().getLocation()))
                    .findFirst()
                    .ifPresent(space -> {
                        user.setCurrentSubject(space);
                        spaceUiTemplate.open(e.getPlayer());
                    });
        }
    }*/
}
