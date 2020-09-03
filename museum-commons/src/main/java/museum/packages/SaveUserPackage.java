package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import museum.data.UserInfo;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class SaveUserPackage extends MuseumPackage {

	// request
	private final UUID user;
	private final UserInfo userInfo;

	// no response

}
