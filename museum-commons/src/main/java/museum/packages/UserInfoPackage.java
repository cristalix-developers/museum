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

	private final UUID uuid;

	private UserInfo userInfo;

	private Action action;

	public enum Action {

		DATA_SYNC,
		DISCONNECT_SAVE,
		DATA_REQUEST

	}

}
