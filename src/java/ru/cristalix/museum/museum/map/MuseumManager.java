package ru.cristalix.museum.museum.map;

import clepto.cristalix.Cristalix;
import clepto.cristalix.WorldMeta;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import ru.cristalix.core.build.models.Box;
import ru.cristalix.core.build.models.IZone;
import ru.cristalix.core.build.models.Point;
import ru.cristalix.core.map.BukkitWorldLoader;
import ru.cristalix.core.map.LoadedMap;
import ru.cristalix.core.map.MapListDataItem;
import ru.cristalix.core.math.V3;
import ru.cristalix.core.util.UtilV3;
import ru.cristalix.museum.App;
import ru.cristalix.museum.data.subject.SubjectType;
import ru.cristalix.museum.museum.subject.SkeletonSubject;
import ru.cristalix.museum.museum.collector.CollectorNavigator;
import ru.cristalix.museum.museum.subject.SimpleSubject;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Getter
public class MuseumManager implements WorldMeta {

    // Защита от [no discrimination] на билдерах
    private static final Map<String, SubjectType> typeMappings = new HashMap<>();

    static {
        typeMappings.put("case-small", SubjectType.SKELETON_CASE);
        typeMappings.put("case-big", SubjectType.SKELETON_CASE);
        typeMappings.put("tree1", SubjectType.DECORATION);
    }

    private final App app;
    private final LoadedMap<World> cristalixMap;
    private final Map<String, MuseumPrototype> museumPrototypeMap = new HashMap<>();
    private final Map<String, SubjectPrototype> subjectPrototypeMap = new HashMap<>();

    public MuseumManager(App app) {
        this.app = app;
        MapListDataItem mapInfo = Cristalix.mapService().getMapByGameTypeAndMapName("MODELS", "Dino")
                .orElseThrow(() -> new RuntimeException("Map museum/main wasn't found in the MapService"));

        try {
            this.cristalixMap = Cristalix.mapService().loadMap(mapInfo.getLatest(), BukkitWorldLoader.INSTANCE).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        for (Point museumPoint : getPoints("museum")) {
            MuseumPrototype prototype = new MuseumPrototype(museumPoint.getTag(), this, point2Loc(museumPoint));
            museumPrototypeMap.put(prototype.getAddress(), prototype);
        }
        getWorld().setGameRuleValue("mobGriefing", "false");

        Map<String, CollectorNavigator> routes = new HashMap<>();
        for (Map.Entry<String, List<Point>> e : getPoints().entrySet()) {
            String key = e.getKey();
            if (!key.startsWith("path")) continue;
            List<Location> points = e.getValue().stream()
                    .sorted(Comparator.comparing(Point::getTag))
                    .map(this::point2Loc)
                    .collect(Collectors.toList());
            Location avg = new Location(getWorld(), 0, 0, 0);
            points.forEach(avg::add);
            Location average = avg.toVector().multiply(1.0 / points.size()).toLocation(getWorld());
            MuseumPrototype prototype = museumPrototypeMap.values().stream()
                    .min(Comparator.comparingDouble(proto -> average.distanceSquared(proto.getOrigin())))
                    .orElseThrow(() -> new RuntimeException("No museum prototypes found!"));
            routes.put(key, new CollectorNavigator(prototype, getWorld(), points));
        }
        routes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .forEach(navigator -> navigator.getMuseumProto().getDefaultCollectorNavigators().add(navigator));

        for (Map.Entry<String, IZone> entry : this.getMeta().getZones().entrySet()) {
            String address = entry.getKey();
            IZone zone = entry.getValue();
            SubjectType subjectType = typeMappings.remove(address);

            if (subjectType == null || !(zone instanceof Box))
            	continue;

            Box box = (Box) zone;

            getPoints("price").stream()
                    .filter(p -> zone.isInZone(p.getV3()))
                    .findFirst()
                    .ifPresent(priceOpt -> {
                        if (subjectType != SubjectType.SKELETON_CASE && subjectType != SubjectType.DECORATION)
                            return;
                        V3 relativeOrigin = box.getMax().clone().subtract(box.getMin());

                        subjectPrototypeMap.put(address, new SubjectPrototype(
                                address,
                                Double.parseDouble(priceOpt.getTag()),
                                subjectType == SubjectType.SKELETON_CASE ? SkeletonSubject::new : SimpleSubject::new,
                                UtilV3.toLocation(box.getMin(), getWorld()),
                                UtilV3.toLocation(box.getMax(), getWorld()),
                                relativeOrigin,
								relativeOrigin.clone().mult(0.5)
                        ));
                    });
        }
    }


    public MuseumPrototype getMuseumPrototype(String address) {
        return museumPrototypeMap.get(address);
    }

    public SubjectPrototype getSubjectPrototype(String address) {
        return subjectPrototypeMap.get(address);
    }

}
