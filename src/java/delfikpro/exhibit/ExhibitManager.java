package delfikpro.exhibit;

import clepto.bukkit.YML;
import clepto.cristalix.WorldMeta;
import org.bukkit.configuration.ConfigurationSection;

public class ExhibitManager {

	private final WorldMeta worldMeta;

	public ExhibitManager(ConfigurationSection config, WorldMeta worldMeta) {
		YML.getList(config, "exhibits", this::deserialize);
		this.worldMeta = worldMeta;
	}

	private Exhibit deserialize(ConfigurationSection section) {
		String title = section.getString("title", "???");
		String type = section.getString("type", "none");
		//noinspection SwitchStatementWithTooFewBranches
		switch (type) {
			case "skeleton":
				return new SkeletonExhibit(title,
						section.getInt("pieces", 3),
						section.getString("address"),
						worldMeta
				);
			default:
				throw new IllegalArgumentException("type " + type + " isn't supported.");
		}




	}

}
