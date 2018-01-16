package mrriegel.classicportals.init;

import mrriegel.classicportals.items.ItemUpgrade;
import mrriegel.limelib.helper.RecipeHelper;
import mrriegel.limelib.item.CommonItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModItems {
	public static final CommonItem upgrade = new ItemUpgrade();

	public static void init() {
		upgrade.registerItem();
		RecipeHelper.addShapelessRecipe(new ItemStack(upgrade, 1, 0), "ingotIron", "dustRedstone", "nuggetGold", "dye", "dye");
		RecipeHelper.addShapelessRecipe(new ItemStack(upgrade, 1, 1), "ingotIron", "dustRedstone", "nuggetGold", Items.COMPASS);
		RecipeHelper.addShapelessRecipe(new ItemStack(upgrade, 1, 2), "ingotIron", "dustRedstone", "nuggetGold", Items.FIREWORKS, Items.FIREWORK_CHARGE);
		RecipeHelper.addShapelessRecipe(new ItemStack(upgrade, 1, 3), "ingotIron", "dustRedstone", "nuggetGold", Blocks.REDSTONE_TORCH, Blocks.STONE_BUTTON);
	}

	public static void initClient() {
		upgrade.initModel();
	}
}
