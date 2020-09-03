package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import museum.data.BoosterInfo;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class GlobalBoostersPackage extends MuseumPackage {

	// request
	private final List<BoosterInfo> boosters;

	// no response

}
