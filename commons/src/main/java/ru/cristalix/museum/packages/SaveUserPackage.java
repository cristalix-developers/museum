package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.museum.data.UserInfo;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class SaveUserPackage extends MuseumPackage {

	// request
	private final UUID user;
	private final UserInfo userInfo;

	// no response

}
