package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class TargetMessagePackage extends MuseumPackage {

	// request
	private final Set<UUID> users;
	private final String jsonMessage;

	// no response

}
