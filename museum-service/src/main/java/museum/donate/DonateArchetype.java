package museum.donate;

import museum.packages.UserTransactionPackage;

public interface DonateArchetype {

	default boolean accept(UserTransactionPackage transaction) {
		return true;
	}

	void grant(UserTransactionPackage transaction);

}
