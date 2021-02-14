package museum.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickaxeType {

	DEFAULT("любительская", 0),
	PROFESSIONAL("профессиональная", 50000),
	PRESTIGE("престижная", 3000000),
	LEGENDARY("легендарная", 100000000);

	private final String name;
	private final double price;

	public PickaxeType getNext() {
		return ordinal() >= PickaxeType.values().length - 1 ? null : PickaxeType.values()[ordinal() + 1];
	}

}
