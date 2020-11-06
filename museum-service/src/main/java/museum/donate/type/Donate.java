package museum.donate.type;

import museum.service.user.ServiceUser;

public interface Donate {

	default String accept(ServiceUser user) {
		return null;
	}

	void grant(ServiceUser user);

}
