package ru.cristalix.museum;

/**
 * @author func 23.05.2020
 * @project Museum
 */
public class MongoManager {/*
    private static MongoCollection<User> mongoCollection;

    public static void connect(String uri, String database, String collection) {
        val codecProvider = PojoCodecProvider.builder()
                .conventions(ImmutableList.of(CLASS_AND_PROPERTY_CONVENTION, ANNOTATION_CONVENTION))
                .register(
                        ClassModel.builder(User.class).enableDiscriminator(true).build(),
                        ClassModel.builder(User.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Museum.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Subject.class).enableDiscriminator(true).build(),
                        ClassModel.builder(SkeletonSubject.class).enableDiscriminator(true).build(),
                        ClassModel.builder(Museum.class).enableDiscriminator(true).build()
                ).automatic(true)
                .build();
        val codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(codecProvider)
        );
        mongoCollection = MongoClients.create(uri)
                .getDatabase(database)
                .withCodecRegistry(codecRegistry)
                .getCollection(collection, User.class);
        Bukkit.getConsoleSender().sendMessage("§aConnected to database successfully.");
    }

    public static CompletableFuture<User> load(String name, String uuid) {
        CompletableFuture<User> archaeologist = new CompletableFuture<>();

        mongoCollection.find(eq("uuid", uuid)).first((result, t) -> {
            if (result == null) {
                result = User.builder()
                        .level(1)
                        .name(name)
                        .uuid(uuid)
                        .money(1_000_000)
                        .exp(0)
                        .excavationCount(0)
                        .breakLess(0)
                        .pickedCoinsCount(0)
                        .lastExcavation(ExcavationType.DIRT)
                        .onExcavation(false)
                        .pickaxeType(PickaxeType.DEFAULT)
                        .elementList(new ArrayList<>())
                        .museumList(Collections.singletonList(new Museum(
                                new Date(),
                                Collections.singletonList(new Hall(
                                        HallTemplateType.DEFAULT.getHallTemplate().getMatrix().get(),
                                        HallTemplateType.DEFAULT,
                                        CollectorType.PRESTIGE
                                )), "Музей в честь " + name,
                                -91, 90, 251
                        ))).build();
                Bukkit.getConsoleSender().sendMessage("§aLogged: " + result);

                archaeologist.complete(result);

                mongoCollection.insertOne(
                        result,
                        (resultVoid, th) -> Bukkit.getConsoleSender().sendMessage("§aLogged: " + resultVoid)
                );
            } else
                archaeologist.complete(result);
        });
        return archaeologist;
    }

    public static void save(User archaeologist) {
        mongoCollection.updateOne(
                eq("uuid", archaeologist.getUuid()),
                new Document("$set", archaeologist),
                (result, t) -> Bukkit.getConsoleSender().sendMessage("§aSaved: " + archaeologist.toString())
        );
    }*/
}
