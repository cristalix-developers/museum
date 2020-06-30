package ru.cristalix.museum.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.packages.ConfigurationsPackage;
import ru.cristalix.museum.socket.ServerSocketHandler;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ConfigurationManager {

	private final String configFile, guiFile, itemsFile;

	private String configData, guiData, itemsData;

	public void init() {
		reload();
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::reload, 1L, TimeUnit.MINUTES);
	}

	private Pair<String, Boolean> load(String file, String data) {
		String readed = read(file);
		if (Objects.equals(readed, data)) {
			return new Pair<>(data, false);
		}
		data = readed;
		return new Pair<>(data, true);
	}

	public void reload() {
		val config = load(configFile, configData);
		configData = config.getKey();

		val gui = load(guiFile, guiData);
		guiData = gui.getKey();

		val items = load(itemsFile, itemsData);
		itemsData = items.getKey();

		if (config.getValue() || gui.getValue() || items.getValue())
			ServerSocketHandler.broadcast(pckg());
	}

	private String read(String file) {
		try {
			return Base64.getEncoder().encodeToString(String.join("\n", Files.readAllLines(new File(file).toPath())).getBytes());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public ConfigurationsPackage pckg() {
		return new ConfigurationsPackage(configData, guiData, itemsData);
	}

	@Getter
	@AllArgsConstructor
	private static class Pair<K, V> {

		private final K key;
		private final V value;

	}
}
