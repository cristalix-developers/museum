package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode (callSuper = false)
@RequiredArgsConstructor
public class BroadcastTitlePackage extends MuseumPackage {

	// request
	private String[] data;
	private int fadeIn, stay, fadeOut;

	// no response

}
