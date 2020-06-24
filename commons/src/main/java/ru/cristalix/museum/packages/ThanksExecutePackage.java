package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ThanksExecutePackage extends MuseumPackage {

	// request
	private UUID user;

	// response
	private long boostersCount; // Количество бустеров, за которые поблагодарил игрок

}
