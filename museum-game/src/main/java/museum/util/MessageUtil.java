package museum.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.App;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.player.User;
import org.bukkit.Bukkit;
import ru.cristalix.core.formatting.Formatting;

import java.text.DecimalFormat;

/**
 * @author func 11.06.2020
 * @project Museum
 */
@UtilityClass
public class MessageUtil {

	private final DecimalFormat MONEY_FORMAT = new DecimalFormat("###,###,###,###,###,###.##");

	public String toMoneyFormat(double money) {
		return MONEY_FORMAT.format(money);
	}

	private final DecimalFormat COSMOCRYSTAL_FORMAT = new DecimalFormat("###,###,###,###,###,###");

	public String toCrystalFormat(double money) {
		return COSMOCRYSTAL_FORMAT.format(money);
	}

	public String getFormattedState(boolean param) {
		if (param)
			return "включён";
		return "выключен";
	}

	public Message find(String locator) {
		return new Message(locator);
	}

	public String get(String locator) {
		return find(locator).getText();
	}

	@Setter
	@Getter
	public static class Message {

		private String text;

		public Message(String locator) {
			val message = App.getApp().getConfig().getString("chat.messages." + locator);
			if (message == null) {
				Bukkit.getLogger().warning("Message not found \"" + locator + "\"");
				return;
			}
			if (message.startsWith("error"))
				text = Formatting.error(message.substring(5));
			else if (message.startsWith("fine"))
				text = Formatting.fine(message.substring(4));
			else
				text = message;
		}

		public Message set(String key, String value) {
			text = text.replace("%" + key.toUpperCase() + "%", value);
			return this;
		}

		public Message set(String key, long value) {
			return set(key, value + "");
		}

		public Message set(String key, double value) {
			return set(key, value + "");
		}

		public void send(User user) {
			MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, text);
		}

	}

}