package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.museum.boosters.Booster;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class GlobalBoostersPackage extends MuseumPackage {

	// request
	private final List<Booster> boosters;

	// no response

}
