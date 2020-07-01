package ru.cristalix.museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MuseumInfo implements Info {

	public final String prototypeAddress;
	public String title;
	public Date creationDate;
	public long views;

	public MuseumInfo(String prototypeAddress) {
	    this.prototypeAddress = prototypeAddress;
	    this.creationDate = new Date();
    }

}
