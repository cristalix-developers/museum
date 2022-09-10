package museum.museum.subject;

import clepto.bukkit.item.ItemBuilder;
import clepto.bukkit.menu.Guis;
import lombok.val;
import lombok.var;
import me.func.mod.Anime;
import me.func.mod.Glow;
import me.func.mod.menu.ReactiveButton;
import me.func.mod.menu.selection.Selection;
import me.func.protocol.GlowColor;
import me.func.protocol.menu.Button;
import museum.multi_chat.ChatType;
import museum.multi_chat.MultiChatUtil;
import museum.museum.map.SkeletonSubjectPrototype;
import museum.museum.subject.skeleton.Skeleton;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.player.User;
import museum.prototype.Managers;
import museum.util.BannerUtil;
import museum.util.MessageUtil;
import museum.util.SubjectLogoUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.formatting.Formatting;
import ru.cristalix.core.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import static museum.museum.map.SubjectType.SKELETON_CASE;
import static museum.museum.subject.Allocation.Action.*;
import static museum.museum.subject.Allocation.Action.DESTROY_DISPLAYABLE;
import static museum.util.Colorizer.applyColor;

public class SubjectGui {
    public static void showSketelonGui(User user, SkeletonSubject subject) {
        val player = user.getPlayer();
        Anime.close(player);

        val buttons = new ArrayList<ReactiveButton>();
        buttons.add(getInformationFragment(subject));
        buttons.add(getSkeletonsListButton(user, subject));
        buttons.add(getColorChangeButton(player, subject));
        buttons.add(getRemoveSubjectButton(player, subject));
        buttons.add(getUpgradeButton(user, subject));

        val menu = new Selection(
                subject.getPrototype().getTitle(),
                "",
                "Открыть",
                3,
                3
        );
        menu.setStorage(buttons);
        menu.setVault("\uE03F");
        menu.setMoney(MessageUtil.toMoneyFormat(user.getMoney()));

        menu.open(player);
    }

    public static void showChangeColorGui(Player player, Subject subject) {
        val buttons = new ArrayList<ReactiveButton>();
        for (val color : Color.values()) {
            val icon = new Items.Builder().type(Material.CONCRETE).color(color).build();

            buttons.add(new ReactiveButton()
                    .item(icon)
                    .title(color.getTeamName())
                    .onClick((click, index, button) -> {
                        subject.getCachedInfo().setColor(color);
                        val allocation = subject.getAllocation();
                        if (allocation != null) {
                            allocation.prepareUpdate(data -> applyColor(data, color));
                            allocation.perform(Allocation.Action.UPDATE_BLOCKS);
                        }
                        if (subject instanceof FountainSubject) {
                            subject.setAllocation(subject.getAllocation());
                        }
                    })
            );
        }

        val menu = new Selection(
                subject.getPrototype().getTitle(),
                "",
                "Использовать",
                4,
                4
        );
        menu.setStorage(buttons);

        menu.open(player);
    }

    public static void getSkeletonListGui(User user, SkeletonSubject subject) {
        val availableButtons = new ArrayList<ReactiveButton>();
        val reservedbuttons = new ArrayList<ReactiveButton>();
        val lockedbuttons = new ArrayList<ReactiveButton>();

        for (val it : Managers.skeleton.stream().sorted(Comparator.comparing(SkeletonPrototype::getTitle)).toArray()) {
            val skeleton = user.getSkeletons().get((SkeletonPrototype) it);

            if (skeleton == null) {
                lockedbuttons.add(new ReactiveButton()
                        .title("Не открыто")
                        .hint("")
                        .texture("minecraft:mcpatcher/cit/others/lock.png"));
                continue;
            }

            val placedOn = user.getMuseums().get(Managers.museum.getPrototype("main")).getSubjects(SKELETON_CASE)
                    .stream().filter(item -> (item.getSkeleton() != null && item.getSkeleton() == skeleton)).findFirst();

            val button = new ReactiveButton()
                    .title(skeleton.getPrototype().getTitle())
                    .description(skeleton.getUnlockedFragments().size() + "/" + skeleton.getPrototype().getFragments().size())
                    .texture("minecraft:textures/items/bone.png");

            if (skeleton.getUnlockedFragments().size() < 3) {
                button.setHint("Слишком\nмало");
                button.setHover("Должно быть минимум 3 фрагмента");
            }
            else if (skeleton.getPrototype().getSize() > ((SkeletonSubjectPrototype)subject.getPrototype()).getSize()) {
                button.setHint("Слишком\nбольшой");
                button.setHover("Скелет cлишком большой для этого стенда");
            }
            else if (placedOn.isPresent() && skeleton == placedOn.get().getSkeleton() && !skeleton.equals(subject.getSkeleton()) ) {
                button.setHint("Уже\nиспользуется");
                button.setHover("Уже используется на другом стенде");
                reservedbuttons.add(button);
                continue;
            }
            else {
                if (skeleton == subject.getSkeleton()) {
                    button.item(new Items.Builder().type(Material.BONE).build());
                    button.setTitle("§b" + button.getTitle());
                    button.setHint("Убрать");
                } else {
                    button.setHint("Поставить");
                }

                button.onClick((click, index, __) -> {
                    Skeleton previousSkeleton = subject.getSkeleton();
                    Allocation allocation = subject.getAllocation();

                    if (allocation != null) {
                        if (previousSkeleton != null) {
                            allocation.perform(HIDE_PIECES);
                            allocation.removePiece(previousSkeleton.getPrototype());
                        }
                        if (subject.getSkeleton() == skeleton)
                            subject.setSkeleton(null);
                        else {
                            for (val item : user.getSubjects()) {
                                if (item instanceof SkeletonSubject && ((SkeletonSubject) item).getSkeleton() == skeleton) {
                                    ((SkeletonSubject) item).setSkeleton(null);
                                    ((SkeletonSubject) item).updateSkeleton(true);
                                    if (item.getAllocation() != null) {
                                        item.getAllocation().perform(HIDE_PIECES);
                                        item.getAllocation().removePiece(skeleton.getPrototype());
                                    }
                                }
                            }
                            subject.setSkeleton(skeleton);
                        }
                        subject.updateSkeleton(true);
                        user.updateIncome();
                        BannerUtil.updateBanners(subject);
                    }

                    Anime.close(user.getPlayer());
                });
            }
            availableButtons.add(button);
        }

        val menu = new Selection(
                "Скелеты",
                "",
                "Открыть",
                5,
                2
        );

        availableButtons.addAll(reservedbuttons);
        availableButtons.addAll(lockedbuttons);
        menu.setStorage(availableButtons);

        menu.open(user.getPlayer());
    }

    public static void showSimpleGui(Player player, Subject subject) {
        Anime.close(player);
        val menu = new Selection(
                subject.getPrototype().getTitle(),
                "",
                "Открыть",
                3,
                3,
                getInformationFragment(subject),
                getColorChangeButton(player, subject),
                getRemoveSubjectButton(player, subject)
        );
        menu.open(player);
    }

    private static ReactiveButton getUpgradeButton(User user, SkeletonSubject subject) {
        val upgradeCost = 10000;
        val upgradePercent = 20;

        val button = new ReactiveButton()
                .texture("minecraft:mcpatcher/cit/others/hub/guild_invite.png")
                .title("Улучшить")
                .price(upgradeCost);

        if (subject.getLevel() >= 50) button.setHint("Максимальная\nпрокачка");
        else if (user.getMoney() < upgradeCost) button.setHint("Не хватает\nденег");
        else {
            button.hover("С каждым уровнем витрина\nприносит на &b" + upgradePercent +
                    "%▲&f больше дохода \n&b" + (subject.getLevel()) +
                    " &fуровень ➠ &b&l" + (subject.getLevel() + 1) +
                    " уровень &a+" + subject.getLevel() * upgradePercent + "% ▲▲▲");
            button.hint("Купить");

            button.onClick((click, index, __) -> {
                user.giveMoney(-upgradeCost);
                subject.setLevel(subject.getLevel() + 1);
                Glow.animate(user.handle(), 3.0, GlowColor.GREEN);
                MultiChatUtil.sendMessage(user.getPlayer(), ChatType.SYSTEM, Formatting.fine("Вы улучшили витрину до §b" + subject.getLevel() + "§f уровня!"));
                BannerUtil.updateBanners(subject);
                user.updateIncome();
                showSketelonGui(user, subject);
            });
        }


        return button;
    }

    private static ReactiveButton getInformationFragment(Subject subject) {
        var description = "Доход: " + String.format("%.2f", subject.getIncome());
        if (subject instanceof SkeletonSubject) {
            val skeletonSubject = ((SkeletonSubject)subject);
            if (skeletonSubject.getSkeleton() != null) description += "\n§b" + skeletonSubject.getSkeleton().getPrototype().getTitle();
        }

        return new ReactiveButton()
            .texture("minecraft:mcpatcher/cit/others/stats.png")
            .title("Информация")
            .description(description)
            .hint("");
    }

    private static ReactiveButton getSkeletonsListButton(User user, SkeletonSubject subject) {
        return new ReactiveButton()
            .texture("minecraft:mcpatcher/cit/others/settings.png")
            .title("Скелеты")
            .onClick((click, index, button) -> getSkeletonListGui(user, subject));
    }

    private static ReactiveButton getColorChangeButton(Player player, Subject subject) {
        return new ReactiveButton()
            .texture("minecraft:mcpatcher/cit/others/clothes.png")
            .title("Изменить цвет")
            .description("Поменять цвет объекта")
            .onClick((click, index, button) -> showChangeColorGui(player, subject));
    }

    private static ReactiveButton getRemoveSubjectButton(Player player, Subject subject) {
        return new ReactiveButton()
            .texture("minecraft:mcpatcher/cit/others/badges/cancel.png")
            .title("Убрать")
            .hint("Убрать")
            .description("Урать объект из музея")
            .onClick((click, index, button) -> {
                val allocation = subject.getAllocation();
                if (allocation == null) return;
                allocation.perform(PLAY_EFFECTS, HIDE_BLOCKS, HIDE_PIECES, DESTROY_DISPLAYABLE);
                BannerUtil.deleteBanners(subject);
                subject.setAllocation(null);

                player.getInventory().addItem(SubjectLogoUtil.encodeSubjectToItemStack(subject));
                Anime.close(player);
            });
    }
}
