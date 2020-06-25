package ru.cristalix.museum.player.prepare;

import org.apache.commons.io.input.ReaderInputStream;
import ru.cristalix.core.display.IDisplayService;
import ru.cristalix.core.display.messages.JavaScriptMessage;
import ru.cristalix.museum.App;
import ru.cristalix.museum.player.User;

import java.io.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			Stream.of(files).forEach(file -> {
				try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
					code.append(reader.lines().collect(Collectors.joining("\n")));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			code.append("})(this);");
			codes = new JavaScriptMessage(new String[]{code.toString()});
		}
		IDisplayService.get().sendScripts(user.getUuid(), codes);
	}
}
