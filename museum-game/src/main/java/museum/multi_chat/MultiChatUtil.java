package museum.multi_chat;

import lombok.val;
import me.func.mod.ui.MultiChat;
import me.func.protocol.data.chat.ModChat;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class MultiChatUtil {
    private static final ArrayList<ModChat> chats = new ArrayList<>();

    public static void createChats() {
        for (val type : ChatType.values()) {
            val chat = new ModChat(
                    UUID.randomUUID(),
                    type.title,
                    type.symbol
            );
            MultiChat.createKey(type.key, chat);
            chats.add(chat);
        }
    }

    public static void removeChats(Player player) {
        ModChat[] multiChats = new ModChat[chats.size()];
        multiChats = chats.toArray(multiChats);
        MultiChat.removeChats(player, multiChats);
    }

    public static void sendMessage(Player player, ChatType type, String... messages) {
        for (val msg : messages) {
            MultiChat.sendMessage(player, type.key, msg);
        }
    }

    public static void sendChats(Player player) {
        ModChat[] multiChats = new ModChat[chats.size()];
        multiChats = chats.toArray(multiChats);
        MultiChat.sendChats(player, multiChats);
    }
}
