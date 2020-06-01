package ru.func.museum.element.deserialized;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author func 01.06.2020
 * @project Museum
 */
public class EntityDeserializer {
    private Gson gson = new Gson();

    public MuseumEntity[] execute(List<String> pool) {
        MuseumEntity[] entities = new MuseumEntity[pool.size()];
        for(int i = 0; i < pool.size(); i++)
            entities[i] = gson.fromJson(pool.get(i), MuseumEntity.class);
        return entities;
    }
}
