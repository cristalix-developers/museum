package ru.func.museum.data;

import lombok.Data;
import ru.func.museum.data.collector.CollectorInfo;
import ru.func.museum.data.subject.SubjectInfo;

import java.util.Date;
import java.util.List;

@Data
public class MuseumInfo {

	public String address;
	public String title;
	public Date creationDate;
	public long views;
	public List<CollectorInfo> collectorInfos;
	public int collectorSlots;
	public List<SubjectInfo> subjectInfos;

}
