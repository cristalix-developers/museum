package ru.cristalix.museum.prototype;

import clepto.ListUtils;
import clepto.bukkit.InvalidConfigException;
import clepto.cristalix.Label;
import clepto.cristalix.MapServiceException;
import lombok.val;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import ru.cristalix.core.formatting.Color;
import ru.cristalix.core.math.D2;
import ru.cristalix.core.math.V3;
import ru.cristalix.museum.data.subject.SubjectInfo;
import ru.cristalix.museum.excavation.ExcavationPrototype;
import ru.cristalix.museum.museum.map.*;
import ru.cristalix.museum.museum.subject.skeleton.Rarity;
import ru.cristalix.museum.museum.subject.skeleton.SkeletonPrototype;

import java.util.*;
import java.util.stream.Collectors;

public class Managers {

    public static PrototypeManager<SubjectPrototype> subject;
    public static PrototypeManager<MuseumPrototype> museum;
    public static PrototypeManager<SkeletonPrototype> skeleton;
    public static PrototypeManager<ExcavationPrototype> excavation;

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
                builder = SkeletonSubjectPrototype.builder()
                        .size(box.requireLabel("size").getTagInt());

            else builder = SubjectPrototype.builder();

            return builder.relativeOrigin(box.toRelativeVector(label.isPresent() ? label.get() : box.getCenter()))
                    .relativeManipulators(box.getLabels("manipulator").stream()
                            .map(box::toRelativeVector)
                            .collect(Collectors.toList())
                    ).address(address)
                    .price(box.getLabels("price").stream()
                            .findAny()
                            .map(Label::getTagDouble)
                            .orElse(Double.NaN)
                    ).cristalixPrice(box.getLabels("cristalix-price").stream()
                            .findAny()
                            .map(Label::getTag)
                            .map(Integer::parseInt)
                            .orElse(0)
                    ).title(box.requireLabel("title").getTag())
                    .box(box)
                    .type(type)
                    .build();
        });

        museum = new PrototypeManager<>("museum", (address, box) -> {
            box.expandVert();
            val defaultInfos = new ArrayList<SubjectInfo>();
            for (Label label : box.getLabels("default")) {
                String[] tag = label.getTag().split(" ");
                SubjectPrototype prototype = subject.getPrototype(tag[0]);
                if (prototype == null)
                    throw new MapServiceException("Illegal default subject '" + tag[0] + "' in museum " +
                            address + " on " + label.getCoords());
                defaultInfos.add(new SubjectInfo(prototype.getAddress(), label.toV3(), D2.PX, Color.AQUA, tag.length > 1 ? tag[1] : null));
            }
            return new MuseumPrototype(address, box.requireLabel("spawn"), defaultInfos);
        });

        skeleton = new PrototypeManager<>("skeleton", (address, box) -> new SkeletonPrototype(
                box.requireLabel("title").getTag(),
                box.requireLabel("size").getTagInt(),
                box.requireLabel("pieces").getTagInt(),
                Rarity.valueOf(box.requireLabel("rarity").getTag().toUpperCase()),
                address,
                box.requireLabel("origin")
        ));

        excavation = new PrototypeManager<>("excavation", (address, box) -> {
            box.expandVert();

            List<int[]> pallette = box.getLabels("pallette").stream()
                    .map(location -> location.add(0, -1, 0).getBlock())
                    .map(block -> new int[]{block.getType().getId(), block.getData()})
                    .collect(Collectors.toList());

            if (pallette.isEmpty())
                throw new MapServiceException("No pallette markers found for excavation " + address);

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
                        Location loc = new Location(box.getMeta().getWorld(), x, y, z);
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
                chunks.add(location.getChunk());
            }

            val packets = chunks.stream()
                    .map(chunk -> new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535))
                    .collect(Collectors.toList());

            space.forEach(block -> block.getBlock().setType(Material.AIR));

            return new ExcavationPrototype(
                    address, skeletonPrototypes,
                    box.requireLabel("hit-count").getTagInt(),
                    box.requireLabel("required-level").getTagInt(),
                    box.requireLabel("price").getTagDouble(),
                    box.requireLabel("title").getTag(),
                    box.requireLabel("spawn"),
                    packets
            );
        });
    }

}
