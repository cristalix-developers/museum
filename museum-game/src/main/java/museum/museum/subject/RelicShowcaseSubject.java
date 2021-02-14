package museum.museum.subject;

import lombok.Getter;
import lombok.val;
import museum.App;
import museum.data.SubjectInfo;
import museum.fragment.Fragment;
import museum.fragment.Gem;
import museum.fragment.Relic;
import museum.museum.Museum;
import museum.museum.map.SubjectPrototype;
import museum.museum.subject.skeleton.AtomPiece;
import museum.museum.subject.skeleton.V4;
import museum.player.User;
import museum.util.MessageUtil;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

/**
 * @author func 11.11.2020
 * @project museum
 */
@Getter
public class RelicShowcaseSubject extends Subject {

	private Fragment fragment;
	private AtomPiece piece;
	private V4 absoluteLocation;
	private int counter = 0;

	public RelicShowcaseSubject(SubjectPrototype prototype, SubjectInfo info, User owner) {
		super(prototype, info, owner);
		this.fragment = info.metadata != null && !info.metadata.isEmpty() ?
				info.metadata.contains(":") ? new Gem(info.metadata) : new Relic(info.metadata) : null;
	}

	@Override
	public void setAllocation(Allocation allocation) {
		super.setAllocation(allocation);
		if (allocation != null) {
			this.updateFragment();
		}
	}

	public void updateFragment() {
		Allocation allocation = this.getAllocation();
		this.updateInfo();
		if (allocation == null)
			return;
		if (fragment == null) {
			if (piece != null)
				allocation.removePiece(piece);
			return;
		}
		absoluteLocation = V4.fromLocation(allocation.getOrigin()).clone().add(.5, .08, .5);
		EntityArmorStand armorStand = new EntityArmorStand(App.getApp().getNMSWorld());
		// todo: добавить кеширование предметов, а то они одни и теже
		armorStand.setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(fragment.getItem()));
		armorStand.setCustomName(prototype.getTitle());
		armorStand.setCustomNameVisible(true);
		this.piece = new AtomPiece(armorStand);
		allocation.allocatePiece(piece, absoluteLocation, false);
		if (owner.getState() == null)
			return;
		((Museum) owner.getState()).updateIncrease();
	}

	public void rotate() {
		if (fragment == null || !isAllocated())
			return;

		counter++;

		if (counter > 10)
			counter = 0;

		absoluteLocation.add(0, Integer.signum(counter - 5) * 0.03, 0);
		absoluteLocation.add(0, 0, 0, 10);
		getAllocation().allocatePiece(piece, absoluteLocation, true);
		getAllocation().perform(Allocation.Action.UPDATE_PIECES);
	}

	@Override
	public void updateInfo() {
		super.updateInfo();
		cachedInfo.metadata = fragment == null ? null : fragment.getAddress();
	}

	@Override
	public double getIncome() {
		if (fragment == null || !isAllocated())
			return 0;
		return fragment.getPrice() / 100D;
	}

	@Override
	public void acceptClick() {
		val player = owner.getPlayer();
		val itemInHand = player.getItemInHand();

		if (itemInHand != null && itemInHand.hasItemMeta()) {
			val nmsItem = CraftItemStack.asNMSCopy(itemInHand);
			if (nmsItem.tag != null && nmsItem.tag.hasKeyOfType("relic", 8)) {
				if (fragment != null)
					MessageUtil.find("relic-in-hand").send(owner);
				else {
					for (Fragment currentRelic : owner.getRelics()) {
						if (currentRelic.getUuid().toString().equals(nmsItem.tag.getString("relic-uuid"))) {
							player.setItemInHand(null);
							currentRelic.remove(owner);
							setFragment(currentRelic);
							updateFragment();
							getAllocation().perform(Allocation.Action.SPAWN_PIECES);
							MessageUtil.find("relic-placed")
									.set("title", currentRelic.getItem().getItemMeta().getDisplayName())
									.send(owner);
							return;
						}
					}
				}
			}
		}
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
		updateInfo();
	}
}
