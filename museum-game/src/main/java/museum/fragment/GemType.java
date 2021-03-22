package museum.fragment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

@Getter
@AllArgsConstructor
public enum GemType {

	RUBY(1.1F, "ruby", "saphire", "⭐⭐⭐ §4Рубин", "ПН", "Бирмандия"),
	SPINEL(0.85F, "spinel", "saphire","⭐⭐ §cШпиннель", "ВТ", "Таджикистан"),
	AMETHYST(0.75F,"crystal_pink", "tanzanit","⭐ §dАметист", "СР", "Мадагаскар"),
	TANZANITE(0.9F,"turmalin", "tanzanit","⭐ §aТанзанит", "ЧТ", "Танзания"),
	EMERALD(1F,"emerald", "tanzanit","⭐⭐ §aИзумруд", "ПТ", "Колумбия"),
	SAPPHIRE(1.2F,"opal", "saphire","⭐⭐⭐ §9Сапфир", "СБ", "Кешемир"),
	BRILLIANT(1.3F,"br", "brilliant","⭐⭐⭐ §fБриллиант", "ВС", "Борнео");

	private final float multiplier;
	private final String texture;
	private final String oreTexture;
	private final String title;
	private final String dayTag;
	private final String location;

	private final static Calendar calendar = Calendar.getInstance();

	public static GemType getActualGem() {
		calendar.setTime(Date.from(LocalDateTime.now(ZoneId.of("Europe/Moscow"))
				.toLocalDate()
				.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant()
		));
		val day = calendar.get(Calendar.DAY_OF_WEEK);
		return GemType.values()[(day == 1 ? 7 : day - 1) - 1];
	}

}
