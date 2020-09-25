package museum.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import museum.packages.ConfigurationsPackage;
import museum.packages.RequestConfigurationsPackage;
import museum.socket.ServerSocketHandler;
import ru.cristalix.core.CoreApi;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class ConfigurationManager {

	private final String configFile, itemsFile;

	private String configData, itemsData;

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
		Pair<String, Boolean> config = load(configFile, configData);
		configData = config.getKey();

		Pair<String, Boolean> items = load(itemsFile, itemsData);
		itemsData = items.getKey();

		if (config.getValue() || items.getValue())
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
		return new ConfigurationsPackage(configData, itemsData);
	}

	public void fillRequest(RequestConfigurationsPackage pckg) {
		pckg.setConfigData(configData);
		pckg.setItemsData(itemsData);
	}

	@Getter
	@AllArgsConstructor
	private static class Pair<K, V> {

		private final K key;
		private final V value;

	}
}
