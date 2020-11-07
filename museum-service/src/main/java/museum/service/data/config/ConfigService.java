package museum.service.data.config;

import lombok.RequiredArgsConstructor;
import museum.service.MuseumService;
import museum.packages.ConfigurationsPackage;
import ru.cristalix.core.CoreApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigService implements IConfigService {

	private final List<Config> configs = new ArrayList<>();
	private final MuseumService museumService;

	public ConfigService(MuseumService service, String... fileNames) {
		this.museumService = service;
		for (String fileName : fileNames) {
			Config config = new Config(fileName);
			config.refresh();
			configs.add(config);
		}
	}

	@Override
	public void enable() {
		configs.forEach(Config::refresh);
		CoreApi.get().getPlatform().getScheduler().runAsyncRepeating(this::reload, 1L, TimeUnit.MINUTES);
	}

	public void reload() {

		boolean changed = false;
		for (Config config : configs) {
			if (config.refresh()) changed = true;
		}

		if (changed) museumService.getServerSocket().broadcast(pckg());
	}

	public ConfigurationsPackage pckg() {
		return new ConfigurationsPackage(configs.stream().collect(Collectors.toMap(Config::getFileName, Config::getContents)));
	}

}
