package museum.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import museum.data.UserInfo;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class DiscordUserInfoPackage extends MuseumPackage {

	// request
	private final String discordID;

	// response
	private UserInfo userInfo;

}
