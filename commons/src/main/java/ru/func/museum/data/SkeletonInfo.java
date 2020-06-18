package ru.func.museum.data;

import lombok.Data;

import java.util.List;

@Data
public class SkeletonInfo {

	private final String address;
	private final List<Integer> unlockedFragmentIds;

}
