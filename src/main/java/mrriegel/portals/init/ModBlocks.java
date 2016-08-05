package mrriegel.portals.init;

import mrriegel.portals.blocks.BlockController;
import mrriegel.portals.blocks.BlockFrame;
import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileFrame;
import mrriegel.portals.tile.TilePortaal;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

	public static final Block portaal = new BlockPortaal();
	public static final Block controller = new BlockController();
	public static final Block frame = new BlockFrame();

	public static void init() {
		GameRegistry.register(portaal);
		GameRegistry.registerTileEntity(TilePortaal.class, "tile." + portaal.getRegistryName().toString());

		GameRegistry.register(controller);
		GameRegistry.register(new ItemBlock(controller).setRegistryName(controller.getRegistryName()));
		GameRegistry.registerTileEntity(TileController.class, "tile." + controller.getRegistryName().toString());

		GameRegistry.register(frame);
		GameRegistry.register(new ItemBlock(frame).setRegistryName(frame.getRegistryName()));
		GameRegistry.registerTileEntity(TileFrame.class, "tile." + frame.getRegistryName().toString());
	}

}
