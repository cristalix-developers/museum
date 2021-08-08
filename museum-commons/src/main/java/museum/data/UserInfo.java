package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import museum.donate.DonateType;
import ru.cristalix.core.math.V3;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInfo implements Unique {

	public final UUID uuid;

	public String prefix;
	public long experience;
	private double money;
	private long timePlayed;
	private PickaxeType pickaxeType;
	public List<MuseumInfo> museumInfos;
	public List<SubjectInfo> subjectInfos;
	public List<SkeletonInfo> skeletonInfos;
	private int excavationCount;
	private long pickedCoinsCount;
	private V3 lastPosition;
	private List<DonateType> donates;
	private List<BoosterInfo> localBoosters;
	private List<String> claimedPlaces;
	private List<String> claimedRelics;
	private double income;
	private boolean darkTheme;
	private long crystal;
	private int hookLevel;
	private int extraSpeed;
	private int extraBreak;
	private double extraChance;
	private long lastTimeRewardClaim;
	private int prefixChestOpened;
	private List<String> prefixes;
	private boolean privileges;
}
