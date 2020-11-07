package museum.service.data.config;

import museum.packages.ConfigurationsPackage;
import ru.cristalix.core.IService;

public interface IConfigService extends IService {

	ConfigurationsPackage createBundle();

}
