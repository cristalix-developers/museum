package ru.cristalix.museum.configuration;

import lombok.RequiredArgsConstructor;
import ru.cristalix.core.CoreApi;
import ru.cristalix.museum.packages.ConfigurationsPackage;
import ru.cristalix.museum.socket.ServerSocketHandler;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ConfigurationManager {

	private final String configFile, guisFile, itemsFile;

	private String configData, guisData, itemsData;

	public void init() {
		reload();
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::reload, 1L, TimeUnit.MINUTES);
	}

	public void reload() {
		boolean reload = false;
		String config = read(configFile);
		if (!config.equals(configData)) {
			configData = config;
			reload = true;
		}
		String guis = read(guisFile);
		if (!guis.equals(guisData)) {
			guisData = guis;
			reload = true;
		}
		String items = read(itemsFile);
		if (!items.equals(itemsData)) {
			itemsData = items;
			reload = true;
		}
		if (reload) {
			ServerSocketHandler.broadcast(pckg());
		}
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
		return new ConfigurationsPackage(configData, guisData, itemsData);
	}

}
