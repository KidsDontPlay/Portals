package mrriegel.portals.proxy;

import mrriegel.limelib.helper.IProxy;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.network.DataMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy implements IProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		ModItems.init();
		ModBlocks.init();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		NetworkRegistry.INSTANCE.registerGuiHandler(Portals.instance, new GuiHandler());
		PacketHandler.registerMessage(DataMessage.class, Side.CLIENT);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

	}

	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if (!event.world.isRemote && event.phase == Phase.START) {
			for (Entity e : event.world.loadedEntityList) {
				if (e instanceof EntityLivingBase || e instanceof EntityItem)
					if(e instanceof EntityPlayerMP)
					System.out.println(e);
					if (e.getEntityData().getInteger("untilPort") > 0) {
						e.getEntityData().setInteger("untilPort", e.getEntityData().getInteger("untilPort") - 1);
					}
			}
		}
	}

}
