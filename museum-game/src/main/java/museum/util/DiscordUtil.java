package museum.util;

import lombok.SneakyThrows;
import museum.client.ClientSocket;
import museum.data.UserInfo;
import museum.packages.DiscordUserInfoPackage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
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
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String ID_OF_BOOST_ROLE = "986624230374387812";
    private static final String ID_OF_NEWS_ROLE = "986910270481903636";
    private static final String ID_OF_EVERYONE_ROLE = "981260794920583188";

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
                .replaceAll("¨......", "")
                .replaceAll("<@.+>", "");

        for (String str : readyMsg.split(" ")) {
            if ((int) readyMsg.charAt(readyMsg.indexOf(str)) > 10000) {
                readyMsg = readyMsg.replace(str + " | ", "").replace(str, "");
            }
        }

        String date = DATE_FORMAT.format(new Date(System.currentTimeMillis() + 3600 * 1000));

        return "[" + date + "] " + readyMsg.replace("_", "\\_").replace("*", "\\*")
                .replace("|", "\\|").replace("@everyone", "").replace("@here", "")
                .replace(ID_OF_EVERYONE_ROLE, "");
    }

    public static String createNormalMessage(String message) {

        String date = DATE_FORMAT.format(new Date(System.currentTimeMillis() + 3600 * 1000));

        return "[" + date + "] " + message.replaceAll("§.", "");
    }

    public static Role getRoleForReactionMessage(Guild guild, String reaction) {
        return "boost".equals(reaction) ? guild.getRoleById(ID_OF_BOOST_ROLE) : guild.getRoleById(ID_OF_NEWS_ROLE);
    }
}
