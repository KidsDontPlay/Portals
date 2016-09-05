package mrriegel.portals.init;

import mrriegel.limelib.item.CommonItem;
import mrriegel.portals.items.ItemUpgrade;

public class ModItems {
	public static final CommonItem upgrade = new ItemUpgrade();

	public static void init() {
		upgrade.registerItem();
	}

	public static void initClient() {
		upgrade.initModel();
	}
}
