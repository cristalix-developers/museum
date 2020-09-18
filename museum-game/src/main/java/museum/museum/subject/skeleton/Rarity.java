package museum.museum.subject.skeleton;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Rarity {
	USUAL(5, "меловой период"),
	RARE(5, "юрский период"),
	AMAZING(5, "триасовый период"),
	FANTASTIC(8, "инопланетное происхождение"),
	;

	private final int rareScale;
	private final String period;
}
