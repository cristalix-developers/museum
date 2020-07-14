package ru.cristalix.museum.player.prepare;

import clepto.cristalix.Scripts;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

import java.io.File;

/**
 * @author func 13.06.2020
 * @project Museum
 */
public class PrepareJSAnime implements Prepare {

	private static final String SCRIPT_PATH = "scripts/";
	private JavaScriptMessage codes;

	@Override
	public void execute(User user, App app) {
		if (codes == null) {
			File dir = new File(SCRIPT_PATH);
			dir.mkdirs();
			File[] files = dir.listFiles();

			if (files == null)
				throw new RuntimeException("Cannot load scripts at " + SCRIPT_PATH + "!");

			this.codes = Scripts.loadAndMerge(files);
		}

		IDisplayService.get().sendScripts(user.getUuid(), codes);
	}
}
