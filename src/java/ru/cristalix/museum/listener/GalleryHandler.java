package ru.cristalix.museum.listener;

import lombok.AllArgsConstructor;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.cristalix.museum.App;
import ru.cristalix.museum.museum.map.SubjectPrototype;
import ru.cristalix.museum.prototype.Managers;

/**
 * @author func 14.07.2020
 * @project museum
 */
@AllArgsConstructor
public class GalleryHandler implements Listener {

	private final App app;

	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {
		val block = event.getClickedBlock();
		if (block == null)
			return;
		if (block.getType() != Material.SIGN_POST)
			return;
		for (SubjectPrototype subjectPrototype : Managers.subject.getMap().values()) {
			if (subjectPrototype.getBox().contains(block.getLocation())) {
				// todo: открытие инвентаря для данного Subject
				break;
			}
		}
	}

}
