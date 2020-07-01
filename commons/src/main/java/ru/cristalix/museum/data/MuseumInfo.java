package ru.cristalix.museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MuseumInfo {

	public String address;
	public String title;
	public Date creationDate;
	public long views;

}
