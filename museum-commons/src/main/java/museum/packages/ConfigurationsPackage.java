package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ConfigurationsPackage extends MuseumPackage {

	private final Map<String, String> elements;

}
