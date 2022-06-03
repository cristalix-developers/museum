package museum.discord;

import museum.discord.events.OnMessageReceivedEvent;
import museum.discord.events.OnReadyEvent;
import museum.util.DiscordUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.chat.BaseComponent;

import javax.security.auth.login.LoginException;

public class Bot {

    private static JDA jda;

    public static void init() {
        JDABuilder builder = JDABuilder.createDefault("OTgxMjYxMzk3NzM0MzMwMzY4.G_kvi6.D4z0EZdg0luSa4BCxQrjKQptiD5h5zA8wdMEBU");
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        builder.addEventListeners(new OnReadyEvent());
        builder.addEventListeners(new OnMessageReceivedEvent());

        try {
            jda = builder.build();
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    public static User getSelfUser() {
        return jda.getSelfUser();
    }

    public static void sendGlobalMessage(String serverName, BaseComponent[] message) {
        jda.getTextChannelById(DiscordUtil.getChannelId(serverName)).sendMessage(DiscordUtil.createGlobalMessage(message)).queue();
    }

    public static void sendNormalMessage(String serverName, String message) {
        jda.getTextChannelById(DiscordUtil.getChannelId(serverName)).sendMessage(DiscordUtil.createNormalMessage(message)).queue();
    }
}
