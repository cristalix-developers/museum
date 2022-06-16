package museum.util;

import lombok.SneakyThrows;
import museum.client.ClientSocket;
import museum.data.UserInfo;
import museum.packages.DiscordUserInfoPackage;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static museum.App.app;

public class DiscordUtil {

    static ClientSocket client = app.getClientSocket();

    @SneakyThrows
    public static UserInfo getGameUser(String discordID) {
        DiscordUserInfoPackage discordUserInfoPackage = client.writeAndAwaitResponse(new DiscordUserInfoPackage(discordID))
                .get(5L, TimeUnit.SECONDS);
        if (discordUserInfoPackage == null)
            return null;
        return discordUserInfoPackage.getUserInfo();
    }

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

        return "[" + date + "] " + readyMsg.replace("_", "\\_").replace("*", "\\*")
                .replace("|", "\\|");
    }

    public static String createNormalMessage(String message) {

        String date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                .format(new Date(System.currentTimeMillis() + 3600 * 1000));

        return "[" + date + "] " + message.replace("_", "\\_").replace("*", "\\*")
                .replace("|", "\\|");
    }

}
