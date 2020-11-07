package museum.service.donate.booster;

import museum.data.BoosterInfo;
import ru.cristalix.core.IService;

import java.util.Collection;

public interface IBoosterService extends IService {

	void addGlobalBooster(BoosterInfo booster);

	Collection<? extends BoosterInfo> getGlobalBoosters();

}
