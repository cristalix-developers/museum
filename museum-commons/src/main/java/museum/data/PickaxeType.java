package museum.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PickaxeType {

	DEFAULT("любительская", 0L),
	PROFESSIONAL("профессиональная", 100000L),
	PRESTIGE("престижная", 500000000L),
	LEGENDARY("легендарная", 10000000000L);

	private final String name;
	private final Long price;

	public PickaxeType getNext() {
		return ordinal() >= PickaxeType.values().length - 1 ? null : PickaxeType.values()[ordinal() + 1];
	}

}
