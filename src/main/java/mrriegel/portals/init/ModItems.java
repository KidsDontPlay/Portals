package mrriegel.portals.init;

import mrriegel.portals.items.ItemUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModItems {
	public static final Item upgrade = new ItemUpgrade();

	public static void init() {
		GameRegistry.register(upgrade);
	}
}
