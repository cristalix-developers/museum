package museum.donate.type;

import lombok.Data;
import museum.MuseumService;
import museum.boosters.BoosterType;
import museum.data.BoosterInfo;
import museum.service.user.ServiceUser;

@Data
public class LocalBoosterDonate implements Donate {

	private final BoosterType boosterType;
	private final long seconds;

	@Override
	public void grant(ServiceUser user) {

		BoosterInfo booster = BoosterInfo.defaultInstance(user.getUuid(), user.getName(), boosterType, seconds, true);
		MuseumService.getInstance().getBoosterService().addGlobalBooster(booster);

	}

	@Override
	public String accept(ServiceUser user) {


		for (BoosterInfo booster : MuseumService.getInstance().getBoosterService().getGlobalBoosters()) {
			if (booster.getType() == boosterType) return "Этот глобальный бустер уже действует";
		}
		return null;
	}


}
