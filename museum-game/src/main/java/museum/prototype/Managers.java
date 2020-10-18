package museum.prototype;

import clepto.ListUtils;
import clepto.bukkit.InvalidConfigException;
import clepto.bukkit.item.Items;
import clepto.bukkit.world.Label;
import clepto.bukkit.world.WorldConfigurationException;
import lombok.experimental.UtilityClass;
import lombok.val;
import museum.data.SubjectInfo;
import museum.excavation.ExcavationPrototype;
import museum.museum.map.*;
import museum.museum.subject.skeleton.Rarity;
import museum.museum.subject.skeleton.SkeletonPrototype;
import museum.util.LocationUtil;
import museum.worker.WorkerUtil;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.util.UtilV3;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class Managers {

	public static PrototypeManager<SubjectPrototype> subject;
	public static PrototypeManager<MuseumPrototype> museum;
	public static PrototypeManager<SkeletonPrototype> skeleton;
	public static PrototypeManager<ExcavationPrototype> excavation;

	private static final String TITLE_FIELD = "title";
	private static final String PRICE_FIELD = "price";

	@SuppressWarnings("deprecation")
	public static void init() {
		subject = new PrototypeManager<>("subject", (address, box) -> {
			val label = box.getLabels("origin").stream().findAny();

			SubjectPrototype.SubjectPrototypeBuilder<?, ?> builder;
			SubjectType<?> type = SubjectType.byString(box.requireLabel("type").getTag());

			if (type == SubjectType.COLLECTOR)
				builder = CollectorSubjectPrototype.builder()
						.radius(box.requireLabel("radius").getTagDouble())
						.speed(box.requireLabel("speed").getTagDouble());

			else if (type == SubjectType.SKELETON_CASE)
				builder = SkeletonSubjectPrototype.builder().size(box.requireLabel("size").getTagInt());

			else if (type == SubjectType.FOUNTAIN)
				builder = FountainPrototype.builder().source(box.requireLabel("source"));

			else if (type == SubjectType.STALL) {
				val npc = WorkerUtil.STALL_WORKER_TEMPLATE;
				val npcSpawn = box.requireLabel("npc").toCenterLocation();
				npc.setLocation(npcSpawn);
				builder = StallPrototype.builder()
						.spawn(npcSpawn)
						.worker(() -> npc);
			}

			else builder = SubjectPrototype.builder();

			// Добавляю блок, на который можно ставить данный Subject
			val ableItem = getUnderItem(box.getLabel("able"));
			val title = box.requireLabel(TITLE_FIELD).getTag();
			double price = box.getLabels(PRICE_FIELD).stream()
					.findAny()
					.map(Label::getTagDouble)
					.orElse(0D);

			ItemStack icon = getUnderItem(box.getLabel("icon"));
			icon = ru.cristalix.core.item.Items.fromStack(icon)
					.displayName("§6" + title + " §7(Описание)")
					.loreLines("", "§7Можно ставить на " + ableItem.getType().name().toLowerCase())
					.build();
			builder.icon(icon);
			builder.able(ableItem.getType());

			return builder.relativeOrigin(box.toRelativeVector(label.isPresent() ? label.get() : box.getCenter()))
					.address(address)
					.price(price)
					.dataForClient(new SubjectPrototype.SubjectDataForClient(
							address,
							title,
							UtilV3.fromVector(box.getMin().toVector()),
							UtilV3.fromVector(box.getMax().toVector()),
							price
					)).cristalixPrice(box.getLabels("cristalix-price").stream()
							.findAny()
							.map(Label::getTag)
							.map(Integer::parseInt)
							.orElse(0)
					).title(title)
					.box(box)
					.type(type)
					.build();
		});

		museum = new PrototypeManager<>("museum", (address, box) -> {
			box.expandVert();
			List<SubjectInfo> defaultInfos = new ArrayList<>();
			for (Label label : box.getLabels("default")) {
				String[] tag = label.getTag().split(" ");
				SubjectPrototype prototype = subject.getPrototype(tag[0]);
				if (prototype == null)
					throw new WorldConfigurationException("Illegal default subject '" + tag[0] + "' in museum " +
							address + " on " + label.getCoords());
				defaultInfos.add(new SubjectInfo(new UUID(0, 0), prototype.getAddress(), UtilV3.fromVector(label.toVector()), D2.PX, tag.length > 1 ? tag[1] : null, -1, Color.LIME));
			}
			return new MuseumPrototype(address, box, box.requireLabel("spawn"), defaultInfos);
		});

		skeleton = new PrototypeManager<>("skeleton", (address, box) -> {
			String title = box.requireLabel(TITLE_FIELD).getTag();
			int size = box.requireLabel("size").getTagInt();
			Rarity rarity = Rarity.valueOf(box.requireLabel("rarity").getTag().toUpperCase());
			Label origin = box.requireLabel("origin");

			box.loadChunks();

			List<ArmorStand> stands = box.getEntities(ArmorStand.class);
			if (stands.isEmpty())
				throw new WorldConfigurationException("Skeleton " + address + " has no bone armorstands!");

			return new SkeletonPrototype(address, title, origin, size, rarity, stands, box.requireLabel(PRICE_FIELD).getTagInt());
		});

		excavation = new PrototypeManager<>("excavation", (address, box) -> {
			box.expandVert();

			List<int[]> pallette = box.getLabels("pallette").stream()
					.map(location -> location.add(0, -1, 0).getBlock())
					.map(block -> {
						int[] data = {block.getType().getId(), block.getData()};
						block.setType(Material.AIR);
						return data;
					}).collect(Collectors.toList());

			if (pallette.isEmpty())
				throw new WorldConfigurationException("No pallette markers found for excavation " + address);

			List<SkeletonPrototype> skeletonPrototypes = box.getLabels("available").stream()
					.map(label -> skeleton.getPrototype(label.getTag()))
					.collect(Collectors.toList());

			if (skeletonPrototypes.isEmpty())
				throw new InvalidConfigException("No available skeletons (.p available) found for excavation " + address);

			val min = box.getMin();
			val max = box.getMax();

			List<Location> space = new ArrayList<>();
			for (int x = (int) min.getX(); x < (int) max.getX(); x++) {
				for (int y = (int) min.getY(); y < (int) max.getY(); y++) {
					for (int z = (int) min.getZ(); z < (int) max.getZ(); z++) {
						Location loc = new Location(box.getWorld(), x, y, z);
						Block block = loc.getBlock();
						if (block.getType() == Material.IRON_BLOCK)
							space.add(loc);
					}
				}
			}

			Set<Chunk> chunks = new HashSet<>();
			for (Location location : space) {
				Block block = location.getBlock();
				int[] random = ListUtils.random(pallette);
				block.setTypeIdAndData(random[0], (byte) random[1], false);
				location.getWorld().loadChunk(location.getChunk());
				chunks.add(location.getChunk());
			}

			for (Chunk chunk : chunks) {
				chunk.getWorld().loadChunk(chunk);
			}

			val packets = chunks.stream()
					.map(chunk -> new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535))
					.collect(Collectors.toList());

			space.forEach(block -> block.getBlock().setType(Material.AIR));

			val palletteName = "§eНужная руда";

			val icon = getUnderItem(box.requireLabel("icon"));
			Items.register("excavation-" + address, CraftItemStack.asNMSCopy(icon));

			return new ExcavationPrototype(
					address, skeletonPrototypes,
					LocationUtil.resetLabelRotation(box.requireLabel("spawn"), 0),
					box.requireLabel("hit-count").getTagInt(),
					box.requireLabel("required-level").getTagInt(),
					box.requireLabel(PRICE_FIELD).getTagDouble(),
					box.requireLabel(TITLE_FIELD).getTag(),
					packets,
					icon,
					pallette.stream()
							.map(pal -> {
								val item = new ItemStack(pal[0], 1, (short) 0, (byte) pal[1]);
								val meta = item.getItemMeta();
								meta.setDisplayName(palletteName);
								item.setItemMeta(meta);
								return item;
							}).toArray(ItemStack[]::new)
			);
		});
	}

	private static ItemStack getUnderItem(Label label) {
		ItemStack icon = null;
		if (label.getTag().isEmpty()) {
			Block iconBlock = label.subtract(0, 1, 0).getBlock();
			iconBlock.getChunk().load();
			if (iconBlock.getType() == Material.CHEST) {
				try {
					icon = ((Chest) iconBlock.getState()).getBlockInventory().getItem(0).clone();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
			if (icon == null)
				icon = iconBlock.getDrops().iterator().next();
			iconBlock.setType(Material.AIR);
		} else
			icon = new ItemStack(Material.valueOf(label.getTag().toUpperCase()));
		return icon;
	}
}
