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

	RUBY("ruby", "saphire", "⭐⭐⭐ §4Рубин", "ПН", "Бирмандия"),
	SPINEL("spinel", "saphire","⭐⭐ §cШпиннель", "ВТ", "Таджикистан"),
	AMETHYST("crystal_pink", "tanzanit","⭐ §dАметист", "СР", "Мадагаскар"),
	TANZANITE("turmalin", "tanzanit","⭐ §aТанзанит", "ЧТ", "Танзания"),
	EMERALD("emerald", "tanzanit","⭐⭐ §aИзумруд", "ПТ", "Колумбия"),
	SAPPHIRE("opal", "saphire","⭐⭐⭐ §9Сапфир", "СБ", "Кешемир"),
	BRILLIANT("br", "brilliant","⭐⭐⭐ §fБриллиант", "ВС", "Борнео");

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
