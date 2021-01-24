package museum.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickaxeType {

	DEFAULT("любительская", 0),
	PROFESSIONAL("профессиональная", 100000),
	PRESTIGE("престижная", 1000000),
	LEGENDARY("легендарная", 50000000);

	private final String name;
	private final double price;

	public PickaxeType getNext() {
		return ordinal() >= PickaxeType.values().length - 1 ? null : PickaxeType.values()[ordinal() + 1];
	}

}
