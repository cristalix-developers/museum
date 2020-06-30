package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class UserChatPackage extends MuseumPackage {

	// request
	private final String jsonMessage;

	// no response

}
