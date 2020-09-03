package museum.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickaxeType {

	DEFAULT("любительская", 0),
	PROFESSIONAL("профессиональная", 10000),
	PRESTIGE("престижная", 100000);

	private final String name;
	private final double price;

	public PickaxeType getNext() {
		return PickaxeType.values()[Math.min(ordinal() + 1, PickaxeType.values().length - 1)];
	}

}
