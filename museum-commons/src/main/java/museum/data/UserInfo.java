package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import museum.donate.DonateType;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfo implements Unique {

	public final UUID uuid;

	public long experience;
	private double money;
	private PickaxeType pickaxeType;
	public List<MuseumInfo> museumInfos;
	public List<SubjectInfo> subjectInfos;
	public List<SkeletonInfo> skeletonInfos;
	private int excavationCount;
	private long pickedCoinsCount;
	private List<DonateType> donates;
    private List<BoosterInfo> localBoosters;

}
