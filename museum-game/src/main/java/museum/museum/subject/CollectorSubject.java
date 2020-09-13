package museum.museum.subject;

import clepto.bukkit.Lemonade;
import lombok.Getter;
import lombok.Setter;
import museum.App;
import museum.data.SubjectInfo;
import museum.museum.collector.CollectorNavigator;
import museum.museum.map.CollectorSubjectPrototype;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.AtomPiece;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class CollectorSubject extends Subject {

	@Getter
	private final int id;
	@Getter
	private final AtomPiece piece;
	@Setter
	private CollectorNavigator navigator;

	private final double radius;
	private final int speed;

	public CollectorSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		Lemonade lemonade = Lemonade.get(this.prototype.getAddress());
		ItemStack item = lemonade == null ? new ItemStack(Material.WORKBENCH) : lemonade.render();
		armorStand.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item));
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
		super.updateInfo();
		cachedInfo.metadata = String.valueOf(id);
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		allocation.allocatePiece(piece, V4.fromLocation(this.getCollectorLocation()), false);
	}

	public void move(long iteration) {
		if (navigator == null)
			return;
		Location location = getLocation(iteration);
		// ToDo: Пофиксить монетки
//		user.getCoins().removeIf(coin -> coin.pickUp(user, location, radius, piece.getStand().id));
		if (isAllocated()) getAllocation().allocatePiece(piece, V4.fromLocation(location), true);
	}

	public Location getCollectorLocation() {
		return this.getLocation(System.currentTimeMillis());
	}

	private Location getLocation(long time) {
		int secondsPerLap = 200000 / speed;
		return navigator == null ? getAllocation().getOrigin().toCenterLocation() : navigator.getLocation(time % secondsPerLap / (double) secondsPerLap);
	}
}
