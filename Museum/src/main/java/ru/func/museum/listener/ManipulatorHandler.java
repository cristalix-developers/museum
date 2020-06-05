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
import ru.cristalix.core.inventory.ClickableItem;
import ru.cristalix.core.inventory.ControlledInventory;
import ru.cristalix.core.inventory.InventoryContents;
import ru.cristalix.core.inventory.InventoryProvider;
import ru.cristalix.core.item.Items;
import ru.func.museum.App;
import ru.func.museum.element.Element;

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

    private ControlledInventory spaceUI = ControlledInventory.builder()
            .provider(new InventoryProvider() {
                          @Override
                          public void init(Player player, InventoryContents contents) {
                              val archaeologist = app.getArchaeologistMap().get(player.getUniqueId());

                              for (int i = 0; i < app.getMuseumEntities().length; i++) {
                                  val entity = app.getMuseumEntities()[i];
                                  val parentId = i;

                                  List<Element> elements = archaeologist.getElementList().stream()
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

                                      for (int j = 0; j < fragments.length; j++) {
                                          boolean contains = false;

                                          for (int k = 0; k < elements.size(); k++) {
                                              if (elements.get(k).getId() == j) {
                                                  contains = true;
                                                  break;
                                              }
                                          }

                                          fragments[j + 1] = "§" + (contains ? "a + " : "c - ") + entity.getSubs()[j].getTitle();
                                      }

                                      clickableItem = ClickableItem.of(item.
                                              displayName("§b" + entity.getTitle() + "§f / Изучен")
                                              .lore(fragments)
                                              .build(), event -> player.closeInventory()
                                      );
                                  }

                                  contents.set(i, clickableItem);
                              }
                          }
                      }
            ).rows(9)
            .title("Выбор экспаната")
            .build();

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType() != Material.SIGN_POST)
                return;

            val archaeologist = app.getArchaeologistMap().get(e.getPlayer().getUniqueId());

          /*  for (Space space : archaeologist.getCurrentMuseum().getMatrix())
                if (space.getManipulator().equals(e.getClickedBlock().getLocation()))
                    spaceUI.open(e.getPlayer());*/
        }
    }
}
