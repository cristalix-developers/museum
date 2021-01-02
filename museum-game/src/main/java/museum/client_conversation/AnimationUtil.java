package museum.client_conversation;

import museum.player.User;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 * @author func 02.01.2021
 * @project museum
 */
public class AnimationUtil {
	public static void topTitle(User user, String text) {
		new ScriptTransfer()
				.string(text)
				.send("museumcast", user);
	}

	public static void topTitle(User user, String text, Object... pasteholders) {
		topTitle(user, String.format(text, pasteholders));
	}

	public static void cursorHighlight(User user, String text) {
		new ScriptTransfer()
				.string(text)
				.send("museumcursor", user);
	}

	public static void cursorHighlight(User user, String text, Object... pasteholders) {
		cursorHighlight(user, String.format(text, pasteholders));
	}

	public static void throwIconMessage(User user, ItemStack itemStack, String text, String subtitle) {
		new ScriptTransfer()
				.item(CraftItemStack.asNMSCopy(itemStack))
				.string(text)
				.string(subtitle)
				.send("itemtitle", user);
	}
}
