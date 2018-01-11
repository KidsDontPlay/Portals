package mrriegel.portals.init;

import mrriegel.limelib.block.CommonBlock;
import mrriegel.portals.blocks.BlockCapa;
import mrriegel.portals.blocks.BlockController;
import mrriegel.portals.blocks.BlockFrame;
import mrriegel.portals.blocks.BlockPortaal;

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
	}

	public static void initClient() {
		portaal.initModel();
		controller.initModel();
		frame.initModel();
		capa.initModel();
	}

}
