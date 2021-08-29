package museum.content;

import lombok.experimental.UtilityClass;
import lombok.val;
import museum.client_conversation.ModTransfer;
import museum.player.User;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

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
				transfer.item(CraftItemStack.asNMSCopy(rewards.getIcon()))
						.string("§7Награда: " + rewards.getTitle())
		);
		transfer.send("museum:weekly-reward", user);
	}
}
