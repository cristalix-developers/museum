package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import museum.boosters.BoosterType;
import museum.utils.MultiTimeBar;
import museum.utils.UtilTime;

import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BoosterInfo implements Unique, MultiTimeBar.MultiBarInstance {

	@EqualsAndHashCode.Include
	private final UUID uuid;
	private final UUID owner;
	private final String ownerName;
	private BoosterType type;
	private final long until;
	private final long time;
	private final double multiplier;
	private final boolean global;

	public static BoosterInfo defaultInstance(UUID user, String userName, BoosterType type, long time, boolean global) {
		return new BoosterInfo(UUID.randomUUID(), user, userName, type, System.currentTimeMillis() + time, time, global ? type.getGlobalMultiplier() : type.getLocalMultiplier(), global);
	}

	@Override
	public double getPercentsOfFullTime() {
		return ((until - System.currentTimeMillis()) / ((double) time)) * 100.0;
	}

	@Override
	public String getTitle() {
		return "§eБустер §a" + getType().getName() + " §eот §b" + ownerName + " §a(" + UtilTime.formatTime(until - System.currentTimeMillis(), false) + ") §b/thx";
	}

	public boolean isExpired() {
		return System.currentTimeMillis() > until;
	}

}
