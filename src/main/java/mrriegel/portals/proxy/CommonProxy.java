package mrriegel.portals.proxy;

import mrriegel.limelib.network.PacketHandler;
import mrriegel.portals.ModConfig;
import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.network.SyncCapaMessage;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {
		ModConfig.refreshConfig(event.getSuggestedConfigurationFile());
		ModBlocks.init();
		ModItems.init();
	}

	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Portals.instance, new GuiHandler());
		PacketHandler.registerMessage(SyncCapaMessage.class, Side.CLIENT);
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

}
