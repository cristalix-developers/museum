package ru.cristalix.museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfo {

	public final UUID uuid;

	public long experience;
	public double money;
	public PickaxeType pickaxeType;
	public List<MuseumInfo> museumInfos;
	public List<SkeletonInfo> skeletonInfos;
	public int excavationCount;
	public long pickedCoinsCount;

}
