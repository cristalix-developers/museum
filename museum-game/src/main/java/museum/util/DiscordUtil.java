package museum.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

public class DiscordUtil {
    public static Long getChannelId(String serverName) {
        if (Objects.equals(serverName, "TET-3"))
            return 982265575583657984L;
        return 981922048446201976L;
    }

    public static String createGlobalMessage(BaseComponent[] message) {

        String readyMsg = Arrays.stream(message)
                .map(TextComponent::toLegacyText)
                .collect(Collectors.joining(""))
                .replaceAll("┃", "|")
                .replaceAll("§.", "")
                .replaceAll("¨......", "");

        for (String str : readyMsg.split(" ")) {
            if ((int) readyMsg.charAt(readyMsg.indexOf(str)) > 10000) {
                readyMsg = readyMsg.replace(str + " | ", "").replace(str, "");
            }
        }

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                .format(new Date(System.currentTimeMillis() + 3600 * 1000));

        return "[" + date + "] " + readyMsg;
    }

    public static String createNormalMessage(String message) {

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                .format(new Date(System.currentTimeMillis() + 3600 * 1000));

        return "[" + date + "] " + message;
    }

}
