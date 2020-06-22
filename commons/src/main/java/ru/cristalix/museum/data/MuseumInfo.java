package ru.cristalix.museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.cristalix.museum.data.subject.SubjectInfo;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class MuseumInfo {

	public String address;
	public String title;
	public Date creationDate;
	public long views;
	public List<CollectorInfo> collectorInfos;
	public int collectorSlots;
	public List<SubjectInfo> subjectInfos;

}
