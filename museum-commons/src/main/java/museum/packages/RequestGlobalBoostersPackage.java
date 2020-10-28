package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import museum.data.BoosterInfo;

import java.util.List;

/**
 * @author func 26.10.2020
 * @project museum
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class RequestGlobalBoostersPackage extends MuseumPackage {

	// no request

	// response
	private List<BoosterInfo> boosters;

}