package mrriegel.portals.proxy;

import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ModItems.init();
		ModBlocks.init();

	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Portals.instance, new GuiHandler());
	}

	public void postInit(FMLPostInitializationEvent event) {

	}

}
