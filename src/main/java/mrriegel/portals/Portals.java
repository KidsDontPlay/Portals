package mrriegel.portals;

import mrriegel.limelib.helper.IProxy;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
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

import org.apache.logging.log4j.Logger;

@Mod(modid = Portals.MODID, name = Portals.MODNAME, version = Portals.VERSION, dependencies = "required-after:LimeLib@[1.0.0,)")
public class Portals {
	public static final String MODID = "portals";
	public static final String VERSION = "1.0.0";
	public static final String MODNAME = "Portals";

	@Instance(Portals.MODID)
	public static Portals instance;
	public static Logger logger;

	@SidedProxy(clientSide = "mrriegel.portals.proxy.ClientProxy", serverSide = "mrriegel.portals.proxy.CommonProxy")
	public static IProxy proxy;

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
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@SubscribeEvent
	public void x(PlayerInteractEvent.RightClickBlock e) {
		if (!e.getWorld().isRemote && e.getEntityPlayer().getHeldItemMainhand() != null && e.getEntityPlayer().getHeldItemMainhand().getItem() == Items.STICK) {
			EntityPlayerMP player = (EntityPlayerMP) e.getEntityPlayer();
			// player.rotationYaw = EnumFacing.HORIZONTALS[new
			// Random().nextInt(EnumFacing.HORIZONTALS.length)].getHorizontalAngle();
			// player.rotationPitch = 0f;
			// player.connection.sendPacket(new
			// SPacketPlayerPosLook(player.posX, player.posY,

			// player.setPositionAndRotation(-261, 66, 202-1,
			// player.rotationYaw, player.rotationPitch);
			// TeleportationHelper.teleportToPos(player, new
			// BlockPos(-261,66,202).north());
		}
	}
}
