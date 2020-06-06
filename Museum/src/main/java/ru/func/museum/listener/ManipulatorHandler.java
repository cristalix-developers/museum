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
import ru.func.museum.museum.space.Space;

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

    private ControlledInventory.Builder spaceUiTemplate = ControlledInventory.builder()
            .rows(6)
            .title("Выбор экспаната");

    private ItemStack clear = Items.builder()
            .displayName("§cОсвободить витрину")
            .lore("", "§7Ваши экспонаты не исчезнут!", "§7Вы их сможете поставить", "§7на другую витрину.")
            .type(Material.BARRIER)
            .build();

    @EventHandler
    public void onBlockClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!e.getClickedBlock().getType().name().contains("SIGN"))
                return;

            val archaeologist = app.getArchaeologistMap().get(e.getPlayer().getUniqueId());
            val museum = archaeologist.getCurrentMuseum();
            for (Space space : museum.getMatrix()) {
                if (space.isManipulator(e.getClickedBlock().getLocation())) {
                    spaceUiTemplate.provider(
                            new InventoryProvider() {
                                @Override
                                public void init(Player player, InventoryContents contents) {
                                    contents.set(0, ClickableItem.of(clear, event -> {
                                        space.getElements().forEach(element -> element.setLocked(false));
                                        space.getElements().clear();
                                        space.hide(archaeologist, player);
                                        museum.updateIncrease();
                                        player.closeInventory();
                                        player.sendMessage("§7[§l§bi§7] Витрина освобождена!");
                                    }));

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

                                            for (int j = 0; j < fragments.length - 1; j++) {
                                                boolean contains = false;

                                                for (int k = 0; k < elements.size(); k++) {
                                                    if (elements.get(k).getId() == j) {
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
                                                            "/" + (fragments.length - 1)
                                                    ).lore(fragments).build(), event -> {
                                                        if (elements.get(0).isLocked()) {
                                                            player.sendMessage("§7[§l§bi§7] Вы не можете выбрать экспонат, он занят другой витриной.");
                                                            player.closeInventory();
                                                            return;
                                                        }
                                                        for (Element element : elements) {
                                                            element.setLocked(true);
                                                            space.getElements().add(element);
                                                        }
                                                        // Перезапуск витрины
                                                        museum.updateIncrease();
                                                        space.hide(archaeologist, player);
                                                        space.show(archaeologist, player);
                                                        player.closeInventory();
                                                        player.sendMessage("§7[§l§bi§7] Экспонат уже на витрине!");
                                                    }
                                            );
                                        }
                                        contents.set(i + 1, clickableItem);
                                    }
                                }
                            }
                    ).build().open(e.getPlayer());
                }
            }
        }
    }
}
