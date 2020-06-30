package ru.cristalix.museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SkeletonInfo {

	private final String address;
	private List<String> unlockedFragmentAddresses;

}
