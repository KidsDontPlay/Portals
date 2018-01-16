package mrriegel.classicportals.proxy;

import mrriegel.classicportals.ClassicPortals;
import mrriegel.classicportals.ModConfig;
import mrriegel.classicportals.gui.GuiHandler;
import mrriegel.classicportals.init.ModBlocks;
import mrriegel.classicportals.init.ModItems;
import mrriegel.classicportals.network.SyncCapaMessage;
import mrriegel.limelib.network.PacketHandler;
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
		NetworkRegistry.INSTANCE.registerGuiHandler(ClassicPortals.instance, new GuiHandler());
		PacketHandler.registerMessage(SyncCapaMessage.class, Side.CLIENT);
	}

	public void postInit(FMLPostInitializationEvent event) {
	}

}
