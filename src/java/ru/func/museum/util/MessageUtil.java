package ru.func.museum.util;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.User;

import java.text.DecimalFormat;

/**
 * @author func 11.06.2020
 * @project Museum
 */
@UtilityClass
public class MessageUtil {

    private static String PREFIX;
    private static DecimalFormat MONEY_FORMAT = new DecimalFormat("###,###,###,###,###,###.##$");

    public static String toMoneyFormat(double money) {
        return MONEY_FORMAT.format(money);
    }

    public static Message find(String locator) {
        if (PREFIX == null)
            PREFIX = App.getApp().getConfig().getString("chat.prefix");

        return new Message(locator);
    }

    @Setter
    @Getter
    public static class Message {
        private String text;

        public Message(String locator) {
            text = PREFIX + App.getApp().getConfig().getString("chat.messages." + locator);
        }

        public Message set(String key, String value) {
            text = text.replace("%" + key.toUpperCase() + "%", value);
            return this;
        }

        public Message set(String key, long value) {
            return set(key, value + "");
        }

        public void send(User user) {
            user.sendMessage(text);
        }
    }
}