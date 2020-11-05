package museum.donate;

import lombok.Data;
import museum.boosters.BoosterType;
import museum.packages.UserTransactionPackage;

@Data
public class GlobalBoosterArchetype implements DonateArchetype {

	private final BoosterType boosterType;

	@Override
	public void grant(UserTransactionPackage transaction) {

	}

}
