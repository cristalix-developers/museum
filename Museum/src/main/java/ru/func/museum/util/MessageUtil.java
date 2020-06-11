package ru.func.museum.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import ru.func.museum.App;

/**
 * @author func 11.06.2020
 * @project Museum
 */
public class MessageUtil {

    private static String PREFIX;

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

        public void send(Player player) {
            player.sendMessage(text);
        }
    }
}