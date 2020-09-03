package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ExtraDepositUserPackage extends MuseumPackage {

	// request
	private final UUID user;
	private final Double sum; // nullable.
	private final Double seconds; // nullable. seconds to deposite (earnPerSec * seconds)

	// no response

}
