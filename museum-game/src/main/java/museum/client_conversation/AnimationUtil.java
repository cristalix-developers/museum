package museum.client_conversation;

import lombok.val;
import museum.App;
import museum.player.User;
import museum.player.prepare.BeforePacketHandler;
import museum.util.LevelSystem;
import museum.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.realm.IRealmService;

import java.text.DecimalFormat;

/**
 * @author func 02.01.2021
 * @project museum
 */
public class AnimationUtil {

    public static void topTitle(User user, String text) {
        generateMessage(text, "museumcast", user);
    }

    public static void topTitle(User user, String text, Object... pasteholders) {
        topTitle(user, String.format(text, pasteholders));
    }

    public static void cursorHighlight(User user, String text) {
        generateMessage(text, "museumcursor", user);
    }

    public static void cursorHighlight(User user, String text, Object... pasteholders) {
        cursorHighlight(user, String.format(text, pasteholders));
    }

    public static void throwIconMessage(User user, ItemStack itemStack, String text, String subtitle) {
        new ModTransfer()
                .item(CraftItemStack.asNMSCopy(itemStack))
                .string(text)
                .string(subtitle)
                .send("itemtitle", user);
    }

    public static void generateMessage(String message, String channel, User user) {
        new ModTransfer()
                .string(message)
                .send(channel, user);
    }

    public static void updateLevelBar(User user) {
        val level = user.getLevel();
        val beforeExperience = LevelSystem.getRequiredExperience(level - 1);
        new ModTransfer()
                .integer(level)
                .integer((int) (user.getExperience() - beforeExperience))
                .integer((int) (LevelSystem.getRequiredExperience(level) - beforeExperience))
                .send("museum:levelbar", user);
    }

    public static void updateMoney(User user) {
        new ModTransfer()
                .string(MessageUtil.toMoneyFormat(user.getMoney()))
                .send("museum:balance", user);
    }

    public static void screenMessage(User user, ItemStack itemStack) {
        new ModTransfer()
                .item(CraftItemStack.asNMSCopy(itemStack))
                .send("museum:screenmessage", user);
    }

    public static void updateOnline(User user) {
        new ModTransfer()
                .integer(IRealmService.get().getOnlineOnRealms("MUSM"))
                .send("museum:online", user);
    }

    public static void updateOnlineAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            val user = App.getApp().getUser(player);

            if (user != null)
                updateOnline(user);
        });
    }

    public static void updateIncome(User user) {
        new ModTransfer()
                .string(new DecimalFormat("#00.0").format(user.getIncome()))
                .send("museum:coinprice", user);
    }

    public static void buyFailure(User user) {
        AnimationUtil.throwIconMessage(user, BeforePacketHandler.EMERGENCY_STOP, "Ошибка", "Недостаточно средств");
        user.closeInventory();
    }

    public static void glowing(User user, int red, int blue, int green) {
        new ModTransfer()
                .integer(red)
                .integer(blue)
                .integer(green)
                .send("museum:glow", user);
    }
}
