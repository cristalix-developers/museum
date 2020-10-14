package museum;

import clepto.bukkit.event.EventContext;
import clepto.bukkit.event.EventContextProxy;
import groovy.lang.Script;
import lombok.Getter;

/**
 * @author func 14.10.2020
 * @project museum
 */
@Getter
public abstract class MuseumScript extends Script implements EventContextProxy {
	private final EventContext eventContext = new EventContext(event -> true);
}
