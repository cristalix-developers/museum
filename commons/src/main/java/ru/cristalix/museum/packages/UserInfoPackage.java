package ru.cristalix.museum.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.museum.boosters.Booster;
import ru.cristalix.museum.data.UserInfo;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserInfoPackage extends MuseumPackage {

	// request
	private final UUID uuid;

	// response
	private UserInfo userInfo;
	private List<Booster> localBoosters;

}
