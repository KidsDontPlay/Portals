package mrriegel.portals;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ModConfig {

	public static Configuration config;

	public static boolean energyNeeded;

	public static void refreshConfig(File file) {
		config = new Configuration(file);
		energyNeeded = config.getBoolean("energyNeeded", config.CATEGORY_GENERAL, true, "Is energy needed to teleport");

		if (config.hasChanged())
			config.save();
	}

}
