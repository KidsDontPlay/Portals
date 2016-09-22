package mrriegel.portals.proxy;

import mrriegel.limelib.helper.IProxy;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ConfigHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.network.DataMessage;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
		ConfigHandler.refreshConfig(event.getSuggestedConfigurationFile());
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
			for (Entity e : event.world.playerEntities) {
				// System.out.println(e.getName());
				if (TileController.portableEntity(e))
					if (e.getEntityData().getInteger("untilPort") > 0) {
						e.getEntityData().setInteger("untilPort", e.getEntityData().getInteger("untilPort") - 1);
					}
			}
			for (Entity e : event.world.loadedEntityList) {
				if (e instanceof EntityPlayerMP)
					System.out.println(e.getName());
				;
				if (TileController.portableEntity(e))
					if (e.getEntityData().getInteger("untilPort") > 0) {
						e.getEntityData().setInteger("untilPort", e.getEntityData().getInteger("untilPort") - 1);
					}
			}
		}
	}

	@SubscribeEvent
	public void join(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (!event.getWorld().isRemote && TileController.portableEntity(e) && e.getEntityData().getBoolean("ported")) {
			e.getEntityData().setInteger("untilPort", TileController.untilPort);
		}
	}

}
