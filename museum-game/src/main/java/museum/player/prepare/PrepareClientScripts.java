package museum.player.prepare;

import clepto.cristalix.Cristalix;
import museum.App;
import museum.player.User;
import ru.cristalix.core.display.messages.JavaScriptMessage;

import java.io.File;
import java.nio.file.Files;
import java.util.stream.Collectors;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareClientScripts implements Prepare {

	public static final Prepare INSTANCE = new PrepareClientScripts();

	private final JavaScriptMessage scriptsMessage;

	public PrepareClientScripts() {
		try {
			scriptsMessage = new JavaScriptMessage(new String[] {
					Files.lines(new File("./scripts/bundle.js").toPath()).collect(Collectors.joining("\n"))
			});
		} catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void execute(User user, App app) {
		Cristalix.displayService().sendScripts(user.getUuid(), scriptsMessage);
	}
}