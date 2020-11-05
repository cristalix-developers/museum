package museum.donate;

import museum.data.BoosterInfo;
import museum.packages.UserTransactionPackage;

import java.util.Collections;

public class BoosterArchetype implements DonateArchetype {

	@Override
	public void grant(UserTransactionPackage transaction) {
		(pckg, info) -> wrapUuid(Collections.singletonList(pckg.getUser())).thenApply(list -> list.get(0)).thenAccept(userName -> {
			boosterManager.push(BoosterInfo.defaultInstance(pckg.getUser(), userName, type, DEFAULT_BOOSTER_TIME, global));
		});
	}

}
