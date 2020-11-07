package museum.service.donate;

import museum.service.user.ServiceUser;

public interface Donate {

	default String accept(ServiceUser user) {
		return null;
	}

	void grant(ServiceUser user);

}
