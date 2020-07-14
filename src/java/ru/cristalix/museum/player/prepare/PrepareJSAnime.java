package ru.cristalix.museum.player.prepare;

import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

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

			StringBuilder code = new StringBuilder("(function (self) {");
			for (File file : files) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
					Iterator<String> iterator = reader.lines().iterator();
					String comment = iterator.next();
					if (comment.startsWith("//")) {
						comment = comment.substring(2).trim();
						String[] ss = comment.split(" ");
						if (ss.length > 0 && ss[0].equals("require")) {
							for (int i = 1; i < ss.length; i++) {
								String dependency = ss[i];
								if (dependency.endsWith(".js")) dependency = dependency.substring(0, dependency.length() - 3);


							}
						}
					}
					code.append(reader.lines().collect(Collectors.joining("\n")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			code.append("})(this);");
			codes = new JavaScriptMessage(new String[]{code.toString()});
		}
		IDisplayService.get().sendScripts(user.getUuid(), codes);
	}
}
