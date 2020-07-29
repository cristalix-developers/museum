package museum.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import museum.data.UserInfo;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserInfoPackage extends MuseumPackage {

	// request
	private final UUID uuid;

	// response
	private UserInfo userInfo;

}
