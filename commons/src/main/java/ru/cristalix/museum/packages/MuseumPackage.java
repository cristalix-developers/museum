package ru.cristalix.museum.packages;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class MuseumPackage {

	private final String id = UUID.randomUUID().toString();

}
