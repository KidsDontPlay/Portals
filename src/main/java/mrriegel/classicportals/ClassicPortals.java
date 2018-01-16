package mrriegel.classicportals;

import java.io.IOException;

import mrriegel.classicportals.proxy.CommonProxy;
import mrriegel.classicportals.util.PortalWorldData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

@Mod(modid = ClassicPortals.MODID, name = ClassicPortals.MODNAME, version = ClassicPortals.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required-after:limelib@[1.7.7,)")
public class ClassicPortals {
	public static final String MODID = "classicportals";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Classic Portals";

	@Instance(ClassicPortals.MODID)
	public static ClassicPortals instance;

	@SidedProxy(clientSide = "mrriegel.classicportals.proxy.ClientProxy", serverSide = "mrriegel.classicportals.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent event) {
		try {
			PortalWorldData.start(event.getServer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event) {
		try {
			PortalWorldData.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
