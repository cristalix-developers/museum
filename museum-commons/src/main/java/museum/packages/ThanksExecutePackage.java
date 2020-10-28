package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ThanksExecutePackage extends MuseumPackage {

	// request
	private final UUID user;

	// response
	private long boostersCount; // Количество бустеров, за которые поблагодарил игрок

}
