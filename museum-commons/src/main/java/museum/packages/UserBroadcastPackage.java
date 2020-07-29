package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class UserBroadcastPackage extends MuseumPackage {

	// request
	private final String[] data;
	private final int fadeIn, stay, fadeOut;

	// no response

}
