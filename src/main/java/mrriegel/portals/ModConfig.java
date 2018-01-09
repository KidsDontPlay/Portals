package mrriegel.portals;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ModConfig {

	public static Configuration config;

	public static void refreshConfig(File file) {
		config = new Configuration(file);

		if (config.hasChanged())
			config.save();
	}

}
