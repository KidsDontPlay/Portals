package mrriegel.portals;

import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.proxy.CommonProxy;
import mrriegel.portals.tile.TileController;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Portals.MODID, name = Portals.MODNAME, version = Portals.VERSION)
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
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IllegalArgumentException, IllegalAccessException {
		proxy.postInit(event);
		System.out.println("zip");
		// Field field = BlockColors.class.getFields()[0];
		// field.setAccessible(true);
		// Object v=field.get(Minecraft.getMinecraft().getBlockColors());
		// System.out.println(v);
	}
	
	@SubscribeEvent
	public void x(PlayerInteractEvent.RightClickBlock e){
		if(!e.getWorld().isRemote&&e.getEntityPlayer().getHeldItemMainhand()!=null&&e.getEntityPlayer().getHeldItemMainhand().getItem()==Items.STICK&&e.getWorld().getBlockState(e.getPos())==ModBlocks.controller){
//			((TileController)e.getWorld().getTileEntity(e.getPos())).scanFrame();
//			e.getWorld().setBlockState(e.getPos().offset(e.getFace()), ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS, e.getFace().getAxis()));
		}
	}

}
