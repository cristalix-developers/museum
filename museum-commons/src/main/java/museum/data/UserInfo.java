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
	public double money;
	public PickaxeType pickaxeType;
	public List<MuseumInfo> museumInfos;
	public List<SubjectInfo> subjectInfos;
	public List<SkeletonInfo> skeletonInfos;
	public int excavationCount;
	public long pickedCoinsCount;
	public List<DonateType> donates;
    private List<BoosterInfo> localBoosters;

}
