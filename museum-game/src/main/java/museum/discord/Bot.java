package museum.discord;

import museum.discord.events.OnReadyEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class Bot {

    private static JDA jda;

    public static void init() {
        JDABuilder builder = JDABuilder.createDefault("OTgxMjYxMzk3NzM0MzMwMzY4.G_kvi6.D4z0EZdg0luSa4BCxQrjKQptiD5h5zA8wdMEBU");
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.addEventListeners(new OnReadyEvent());

        try {
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static User getSelfUser() {
        return jda.getSelfUser();
    }

    public static void sendMessage(BaseComponent[] message) {
        jda.getTextChannelById(981922048446201976L).sendMessage(createNormalMessage(message)).queue();
    }

    private static String createNormalMessage(BaseComponent[] message) {

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
}
