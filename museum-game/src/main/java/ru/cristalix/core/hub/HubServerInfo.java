package ru.cristalix.core.hub;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.realm.RealmStatus;

@Data
@RequiredArgsConstructor
public class HubServerInfo {

	private final String realmType;
	private final int realmId;
	private final int online;
	private final int slots;
	private final int extraSlots;
	private final RealmStatus status;

}
