package ru.func.museum.data;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserInfo {

	public final String name;
	public final UUID uuid;

	public long experience;
	public double money;
	public String lastExcavation;
	public PickaxeType pickaxeType;
	public List<MuseumInfo> museumInfos;
	public List<SkeletonInfo> skeletonInfos;
	public int excavationCount;
	public long pickedCoinsCount;

}
