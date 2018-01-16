package mrriegel.classicportals.init;

import mrriegel.classicportals.blocks.BlockCapa;
import mrriegel.classicportals.blocks.BlockController;
import mrriegel.classicportals.blocks.BlockFrame;
import mrriegel.classicportals.blocks.BlockPortaal;
import mrriegel.limelib.block.CommonBlock;
import mrriegel.limelib.helper.RecipeHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ModBlocks {

	public static final CommonBlock portaal = new BlockPortaal();
	public static final CommonBlock controller = new BlockController();
	public static final CommonBlock frame = new BlockFrame();
	public static final CommonBlock capa = new BlockCapa();

	public static void init() {
		portaal.registerBlock();
		controller.registerBlock();
		frame.registerBlock();
		capa.registerBlock();
		RecipeHelper.addShapedRecipe(new ItemStack(frame.getItemBlock(), 4), "qiq", "i i", "qiq", 'q', "gemQuartz", 'i', "ingotIron");
		RecipeHelper.addShapelessRecipe(new ItemStack(controller.getItemBlock()), frame.getItemBlock(), "gemDiamond", Items.ENDER_EYE);
		RecipeHelper.addShapelessRecipe(new ItemStack(capa.getItemBlock()), frame.getItemBlock(), "enderpearl", "chestWood", "blockRedstone", Items.BUCKET);

	}

	public static void initClient() {
		portaal.initModel();
		controller.initModel();
		frame.initModel();
		capa.initModel();
	}

}
