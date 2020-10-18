package museum.museum.subject;

import clepto.bukkit.item.Items;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.Museum;
import museum.museum.collector.CollectorNavigator;
import museum.museum.map.CollectorSubjectPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.AtomPiece;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;

public class CollectorSubject extends Subject implements Incomeble {

	@Getter
	private final int id;
	@Getter
	private final AtomPiece piece;
	@Setter
	private CollectorNavigator navigator;
	@Getter
	private final double radius;
	private final int speed;

	public CollectorSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		// todo: добавить кеширование предметов, а то они одни и теже
		armorStand.setSlot(EnumItemSlot.HEAD, Items.render(this.prototype.getAddress()));
		armorStand.setCustomName(prototype.getTitle());
		armorStand.setCustomNameVisible(true);
		this.navigator = null;
		this.piece = new AtomPiece(armorStand);
		this.id = info.getMetadata() == null ? 0 : Integer.parseInt(info.getMetadata());
		this.speed = (int) ((CollectorSubjectPrototype) prototype).getSpeed();
		this.radius = ((CollectorSubjectPrototype) prototype).getRadius();
	}

	@Override
	public void updateInfo() {
		cachedInfo.metadata = String.valueOf(id);
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (allocation != null) allocation.allocatePiece(piece, V4.fromLocation(this.getCollectorLocation()), false);
	}

	public void move(long iteration) {
		if (navigator == null || !isAllocated())
			return;
		Location location = getLocation(iteration);

		if (getAllocation().getState() instanceof Museum)
			((Museum) getAllocation().getState()).getCoins()
					.removeIf(coin -> coin.pickUp(owner, location, radius, piece.getStand().id));
		getAllocation().allocatePiece(piece, V4.fromLocation(location), true);
	}

	public Location getCollectorLocation() {
		return this.getLocation(System.currentTimeMillis());
	}

	private Location getLocation(long time) {
		int secondsPerLap = 200000 / speed;
		return navigator == null ? getAllocation().getOrigin().toCenterLocation() : navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}

	@Override
	public void handle(double... args) {
		if (args[0] % (90 * 20L) != 0)
			return;
		val income = Math.random() * 100 + 100;
		MessageUtil.find("collector-income")
				.set("income", MessageUtil.toMoneyFormat(income))
				.send(owner);
		owner.setMoney(owner.getMoney() + income);
	}
}
