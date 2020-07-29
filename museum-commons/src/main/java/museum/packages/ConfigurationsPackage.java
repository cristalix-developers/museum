package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ConfigurationsPackage extends MuseumPackage {

	// request
	private final String configData, guisData, itemsData;

	// no response

}
