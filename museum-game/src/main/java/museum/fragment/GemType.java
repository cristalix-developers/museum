package museum.fragment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GemType {

	RUBY("ruby", "⭐⭐⭐ §4Рубин"),
	EMERALD("emerald", "⭐⭐ §aИзумруд"),
	TANZANITE("turmalin", "⭐ §aТанзанит"),
	BRILLIANT("br", "⭐⭐⭐ §fБриллиант"),
	AMETHYST("crystal_pink", "⭐ §dАметист"),
	SAPPHIRE("opal", "⭐⭐⭐ §9Сапфир"),
	SPINEL("spinel", "⭐⭐ §cШпиннель"),;

	private final String texture;
	private final String title;

}
