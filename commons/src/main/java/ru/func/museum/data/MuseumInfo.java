package ru.func.museum.data;

import lombok.Data;
import ru.func.museum.data.collector.CollectorInfo;
import ru.func.museum.data.space.SpaceInfo;

import java.util.Date;
import java.util.List;

@Data
public class MuseumInfo {

	public String title;
	public Date creationDate;
	public long views;
	public List<CollectorInfo> collectorInfos;
	public int collectorSlots;
	public List<SpaceInfo> spaceInfos;

}
