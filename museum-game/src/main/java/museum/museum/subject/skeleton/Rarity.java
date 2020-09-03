package museum.museum.subject.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rarity {
	USUAL(5, "Обыная", "новый"),
	RARE(5, "Редкая", "редкий"),
	AMAZING(5, "Феноменальная", "феноменальный"),
	FANTASTIC(8, "Неизведанная", "ранее неизвестный"),
	;

	private final int rareScale;
	private final String name;
	private final String word;
}
