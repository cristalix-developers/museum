package museum.ticker.detail;

import clepto.bukkit.B;
import implario.ListUtils;
import lombok.val;
import me.func.mod.Anime;
import me.func.mod.conversation.ModTransfer;
import museum.App;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.museum.subject.skeleton.Fragment;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.PlayerDataManager;
import museum.prototype.Managers;
import museum.ticker.Ticked;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * @author Рейдж 23.08.2021
 * @project museum
 */
public class Auction implements Ticked {

    private static final int DURATION = 120;
    private int secondsLeft = DURATION;

    private static final int WAIT_SECOND = 900;
    private int beforeStart = WAIT_SECOND;

    private SkeletonPrototype proto;
    private final PlayerDataManager playerDataManager = App.getApp().getPlayerDataManager();

    private ItemStack itemStack = new ItemStack(Material.CLAY_BALL);


    public Auction() {
        val nmsItem = CraftItemStack.asNMSCopy(itemStack);

        nmsItem.tag = new NBTTagCompound();
        nmsItem.tag.setString("other", "villager");

        itemStack = nmsItem.asBukkitMirror();
    }

    @Override
    public void tick(int... args) {
        if (args[0] % 20 == 0) {
            if (secondsLeft == 0 && beforeStart == 0) {
                secondsLeft = DURATION;
                beforeStart = WAIT_SECOND;

                playerDataManager.setRateBegun(true);
                proto = ListUtils.random(new ArrayList<>(Managers.skeleton));

                B.bc(Formatting.fine("Житель нашёл кость динозавра §b" + proto.getTitle() + ". §fУчаствовать в торгах §b/rate"));
                val message = new ModTransfer()
                        .string("Торги! Сделать ставку §b/rate <сумма>")
                        .integer(DURATION);
                Bukkit.getOnlinePlayers().stream()
                        .map(player -> App.getApp().getUser(player))
                        .forEach(user -> {
                            message.send("museum:tradingtime", user.handle());
                            if (user.isMessages())
                                Anime.itemTitle(user.handle(), itemStack, "Торги!", "Участвовать в торгах /rate");
                        });
            } else if (secondsLeft == 0) {
                if (beforeStart == WAIT_SECOND) {
                    if (playerDataManager.getMembers().isEmpty()) {
                        B.bc(Formatting.fine("Ни кто не участвовал в торгах. Кость осталась у жителя."));
                    } else {
                        val maxEntry = Collections.max(playerDataManager.getMembers().entrySet(), Map.Entry.comparingByValue());
                        val user = App.getApp().getUser(Bukkit.getPlayer(maxEntry.getKey()));

                        Fragment fragment = ListUtils.random(proto.getFragments().toArray(new Fragment[0]));
                        Skeleton skeleton = user.getSkeletons().supply(proto);

                        user.setMoney(user.getMoney() - maxEntry.getValue());

                        MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.fine("Вы победили в торгах и выиграли кость динозавра§b" + proto.getTitle() + "§f."));
                        skeleton.getUnlockedFragments().add(fragment);

                        playerDataManager.getMembers().clear();
                    }
                    playerDataManager.setRateBegun(false);
                }
                beforeStart--;
            } else {
                secondsLeft--;
            }
        }
    }
}
