package ru.func.museum.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.inventory.ClickableItem;
import ru.cristalix.core.inventory.ControlledInventory;
import ru.cristalix.core.inventory.InventoryContents;
import ru.cristalix.core.inventory.InventoryProvider;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.element.Element;
import ru.func.museum.util.MessageUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author func 05.06.2020
 * @project Museum
 */
@RequiredArgsConstructor
public class ManipulatorHandler implements Listener {

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
                              val space = user.getCurrentSpace();

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
            .title("Выбор экспаната")
            .build();

    private ItemStack clear = Items.builder()
            .displayName("§cОсвободить витрину")
            .lore("", "§7Ваши экспонаты НЕ исчезнут!", "§7Вы их сможете поставить", "§7на другую витрину.")
            .type(Material.BARRIER)
            .build();

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!e.getClickedBlock().getType().name().contains("SIGN"))
                return;
            val user = app.getUser(e.getPlayer().getUniqueId());
            user.getCurrentMuseum().getSpaces().stream()
                    .filter(space -> space.isManipulator(e.getClickedBlock().getLocation()))
                    .findFirst()
                    .ifPresent(space -> {
                        user.setCurrentSpace(space);
                        spaceUiTemplate.open(e.getPlayer());
                    });
        }
    }
}
