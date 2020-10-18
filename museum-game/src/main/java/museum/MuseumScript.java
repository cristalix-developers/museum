package museum;

import clepto.bukkit.command.CommandManager;
import clepto.bukkit.command.Commands;
import clepto.bukkit.event.EventContext;
import clepto.bukkit.event.EventContextProxy;
import groovy.lang.Script;
import lombok.Getter;

/**
 * @author func 14.10.2020
 * @project museum
 */
@Getter
public abstract class MuseumScript extends Script implements EventContextProxy, CommandManager.Proxy {
	private final CommandManager commandManager = Commands.getManager();
	private final EventContext eventContext = new EventContext(event -> true);
}
