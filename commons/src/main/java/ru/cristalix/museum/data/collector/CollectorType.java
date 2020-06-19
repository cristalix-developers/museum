package ru.cristalix.museum.data.collector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CollectorType {

	AMATEUR("любительский", 25, 100_000, 1.5, -1),
	PROFESSIONAL("профессиональный", 12, 400_000, 2, 69),
	PRESTIGE("престижный", 6, 750_000, 3, 99);

	private final String name;
	private final int secondsPerLap;
	private final int cost;
	private final double radius;
	private final int cristalixCost;



}
