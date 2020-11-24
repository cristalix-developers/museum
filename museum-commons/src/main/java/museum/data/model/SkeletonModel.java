package museum.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SkeletonModel implements Model {

	private final UUID uuid;
	private final String prototypeAddress;
	private List<String> unlockedFragmentAddresses;

}
