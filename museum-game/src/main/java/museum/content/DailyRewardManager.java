package museum.content;

import lombok.experimental.UtilityClass;
import lombok.val;
import me.func.mod.conversation.ModTransfer;
import museum.player.User;

import java.util.stream.Stream;

/**
 * @author Рейдж 26.08.2021
 * @project museum
 */

@UtilityClass
public class DailyRewardManager {

	public void open(User user) {
		val transfer = new ModTransfer().integer(user.getDay() + 1);
		Stream.of(WeekRewards.values()).forEach(rewards ->
				transfer.item(rewards.getIcon()).string("§7Награда: " + rewards.getTitle())
		);
		transfer.send("museum:weekly-reward", user.handle());
	}
}
