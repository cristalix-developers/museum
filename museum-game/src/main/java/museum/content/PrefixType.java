package museum.content;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrefixType {
    LOVE("䂋", "Любовь", 3, "§l40% §fполучить +§b1 опыт"),
    FORMER_BUM("㧥", "Бывший бомж", 3, "§f+§620`000$ §e ежедневной награды"),
    MOON("㕐", "§bПопаду на луну", 3, "§b+20% §fшанса получить камень"),
    DEAD_INSIDE("㫐", "Dead inside", 2, ""),
    RAINBOW("㕄", "Радуга", 2, ""),
    CIRCUS("㗤", "§eЦирк", 2, ""),
    HIT("㩑", "§lHIT!", 2, ""),
    GOAL("䀝", "§aЦель 40рб", 2, ""),
    NOT_SMOKE("㯨", "Не курю!", 1, ""),
    ALIEN("㥗", "Я пришелец", 1, ""),
    STONKS("㧵", "§aSTONKS", 1, ""),
    BOMB("㧋", "§cБомба", 1, ""),
    OK("㫩", "Ок", 1, ""),
    SPORTS("䀰", "Спортивный", 1, ""),
    MUSHROOM("㕾", "§cМухомор", 1, ""),
    TOXIC("䀀", "§atoxic", 1, ""),
    CONSOLER("㗨", "Консольщик", 1, ""),
    HYPICRITE("㗧", "Лицемер", 1, ""),
    AY("㛳", "АУ", 1, ""),
    LOVE_MUSIC("㗯", "Люблю музыку", 1, ""),
    ;

    private final String prefix;
    private final String title;
    private final int rare;
    private final String bonus;
}
