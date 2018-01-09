package mrriegel.portals;

import mrriegel.portals.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Portals.MODID, name = Portals.MODNAME, version = Portals.VERSION, acceptedMinecraftVersions = "[1.12,1.13)", dependencies = "required-after:limelib@[1.7.7,)")
public class Portals {
	public static final String MODID = "portals";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Portals";

	@Instance(Portals.MODID)
	public static Portals instance;

	@SidedProxy(clientSide = "mrriegel.portals.proxy.ClientProxy", serverSide = "mrriegel.portals.proxy.CommonProxy")
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

}
