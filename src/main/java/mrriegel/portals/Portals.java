package mrriegel.portals;

import java.util.Random;

import mrriegel.portals.proxy.CommonProxy;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileFrame;
import mrriegel.portals.tile.TilePortaal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.Sets;

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
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	@SubscribeEvent
	public void x(PlayerInteractEvent.RightClickBlock e) {
		if (!e.getWorld().isRemote && e.getEntityPlayer().getHeldItemMainhand() != null && e.getEntityPlayer().getHeldItemMainhand().getItem() == Items.STICK) {
			EntityPlayerMP player = (EntityPlayerMP) e.getEntityPlayer();
			player.rotationYaw = EnumFacing.HORIZONTALS[new Random().nextInt(EnumFacing.HORIZONTALS.length)].getHorizontalAngle();
			player.rotationPitch = 0f;
			((EntityPlayerMP) player).connection.sendPacket(new SPacketPlayerPosLook(player.posX, player.posY, player.chasingPosZ, player.rotationYaw, player.rotationPitch, Sets.<SPacketPlayerPosLook.EnumFlags> newHashSet(), 1000));
			// System.out.println(FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(-1).getBlockState(BlockPos.ORIGIN.west(400)).getBlock());
			// if (e.getWorld().getTileEntity(e.getPos()) instanceof TilePortaal
			// && ((TilePortaal)
			// e.getWorld().getTileEntity(e.getPos())).getController() != null)
			// {
			// BlockPos pp = ((TileController)
			// e.getWorld().getTileEntity(((TilePortaal)
			// e.getWorld().getTileEntity(e.getPos())).getController())).getSelfLanding();
			// e.getEntityPlayer().setPositionAndUpdate(pp.getX() + .5,
			// pp.getY() + .05, pp.getZ() + .5);
			// System.out.println("aber");
			// }
			// ((TileController)e.getWorld().getTileEntity(e.getPos())).scanFrame();
			// e.getWorld().setBlockState(e.getPos().offset(e.getFace()),
			// ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS,
			// e.getFace().getAxis()));
		}
	}

//	 @SubscribeEvent
	public void u(LivingUpdateEvent e) {
		if (e.getEntityLiving() instanceof EntityPlayer)
			System.out.println("vor: " + e.getEntityLiving().motionX);
		if (true)
			return;
		if (!e.getEntityLiving().worldObj.isRemote) {
			if (e.getEntityLiving().getEntityData().getInteger("untilPort") > 0) {
				e.getEntityLiving().getEntityData().setInteger("untilPort", e.getEntityLiving().getEntityData().getInteger("untilPort") - 1);
			}
			// System.out.println("hoh: "+e.getEntityLiving().getEntityData().getInteger("untilPort"));
		}
	}

	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if (!event.world.isRemote && event.phase == Phase.START) {
			for (Entity e : event.world.loadedEntityList) {
				if (e instanceof EntityLivingBase || e instanceof EntityItem)
					if (e.getEntityData().getInteger("untilPort") > 0) {
						e.getEntityData().setInteger("untilPort", e.getEntityData().getInteger("untilPort") - 1);
					}
				// System.out.println("hoh: "+e.getEntityLiving().getEntityData().getInteger("untilPort"));
			}
		}
	}

}
