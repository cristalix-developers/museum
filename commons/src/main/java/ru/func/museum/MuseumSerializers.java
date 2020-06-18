package ru.func.museum;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.cristalix.core.GlobalSerializers;
import ru.func.museum.data.SkeletonInfo;
import ru.func.museum.data.space.SpaceInfo;

public class MuseumSerializers {

	public static void initialize() {
		GlobalSerializers.configure(builder -> {
			builder.registerTypeAdapter(SpaceInfo.class, (JsonDeserializer<SpaceInfo>) (jsonElement, type, ctx) -> {
				JsonObject json = jsonElement.getAsJsonObject();
				String spaceType = json.get("type").getAsString();
				JsonElement data = json.get("info");
				if (spaceType.equals("skeleton")) return ctx.deserialize(data, SkeletonInfo.class);
				else return new SpaceInfo();
			});
		});
		GlobalSerializers.bake();
	}

}
