package mrriegel.portals;

import mrriegel.portals.proxy.CommonProxy;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileFrame;
import mrriegel.portals.tile.TilePortaal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = Portals.MODID, name = Portals.MODNAME, version = Portals.VERSION)
public class Portals {
	public static final String MODID = "portals";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Portals";

	@Instance(Portals.MODID)
	public static Portals instance;
	public static Logger logger;

	@SidedProxy(clientSide = "mrriegel.portals.proxy.ClientProxy", serverSide = "mrriegel.portals.proxy.CommonProxy")
	public static CommonProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
		logger = event.getModLog();
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
	public void x(PlayerInteractEvent.RightClickBlock e) {
		if (!e.getWorld().isRemote && e.getEntityPlayer().getHeldItemMainhand() != null && e.getEntityPlayer().getHeldItemMainhand().getItem() == Items.STICK) {
			if (e.getWorld().getTileEntity(e.getPos()) instanceof TilePortaal && ((TilePortaal) e.getWorld().getTileEntity(e.getPos())).getController() != null) {
				BlockPos pp = ((TileController) e.getWorld().getTileEntity(((TilePortaal) e.getWorld().getTileEntity(e.getPos())).getController())).getSelfLanding();
				e.getEntityPlayer().setPositionAndUpdate(pp.getX() + .5, pp.getY() + .05, pp.getZ() + .5);
				System.out.println("aber");
			}
			// ((TileController)e.getWorld().getTileEntity(e.getPos())).scanFrame();
			// e.getWorld().setBlockState(e.getPos().offset(e.getFace()),
			// ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS,
			// e.getFace().getAxis()));
		}
	}
	@SubscribeEvent
	public void u(LivingUpdateEvent e){
		if(!e.getEntityLiving().worldObj.isRemote&&e.getEntityLiving() instanceof EntityPlayer){
//			System.out.println("hoh: "+e.getEntityLiving().motionY);
		}
	}

}
