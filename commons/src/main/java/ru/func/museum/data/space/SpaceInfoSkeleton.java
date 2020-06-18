package ru.func.museum.data.space;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class SpaceInfoSkeleton extends SpaceInfo {

	private final String skeletonAddress;

}
