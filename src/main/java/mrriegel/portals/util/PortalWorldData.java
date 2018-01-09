package mrriegel.portals.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.Portals;
import mrriegel.portals.network.SyncCapaMessage;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@EventBusSubscriber(modid = Portals.MODID)
public class PortalWorldData implements INBTSerializable<NBTTagCompound> {

	public World world;
	public Set<BlockPos> validControllers = new HashSet<>();
	public Map<String, GlobalBlockPos> posMap = new HashMap<>();

	public PortalWorldData() {
	}

	private void update() {
	}

	public void add(BlockPos pos) {
		validControllers.add(pos);
		sync(null);
	}

	public void remove(BlockPos pos) {
		validControllers.remove(pos);
		sync(null);
	}

	public void sync(EntityPlayer player) {
		for (EntityPlayer p : player != null ? Collections.singletonList(player) : world.playerEntities) {
			PacketHandler.sendTo(new SyncCapaMessage(this), (EntityPlayerMP) p);
		}
	}

	public boolean validPos(BlockPos pos) {
		return validControllers.contains(pos);
	}

	public List<String> getNames() {
		validate();
		return validControllers.stream().filter(this::validPos).map(p -> ((TileController) world.getTileEntity(p)).getName()).collect(Collectors.toList());
	}

	public TileController getTile(String name) {
		validate();
		return validControllers.stream().filter(p -> validPos(p) && ((TileController) world.getTileEntity(p)).getName().equals(name)).map(p -> ((TileController) world.getTileEntity(p))).findAny().orElse(null);
	}

	public boolean nameOccupied(String name, BlockPos pos) {
		validate();
		return validControllers.stream().filter(p -> validPos(p) && !pos.equals(p)).map(p -> ((TileController) world.getTileEntity(p))).anyMatch(t -> t.getName().equals(name));
	}

	public void validate() {
		validControllers.removeIf(p -> {
			TileEntity t = world.getTileEntity(p);
			if (!(t instanceof TileController))
				return true;
			return !((TileController) t).isValid();
		});
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTHelper.setList(nbt, "controllers", new ArrayList<>(validControllers));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		validControllers = new HashSet<>(NBTHelper.getList(nbt, "controllers", BlockPos.class));
	}

	@CapabilityInject(PortalWorldData.class)
	public static Capability<PortalWorldData> portalData = null;
	public static final ResourceLocation LOCATION = new ResourceLocation(Portals.MODID, "worldPortals");

	public static void register() {
		CapabilityManager.INSTANCE.register(PortalWorldData.class, new IStorage<PortalWorldData>() {

			@Override
			public NBTBase writeNBT(Capability<PortalWorldData> capability, PortalWorldData instance, EnumFacing side) {
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<PortalWorldData> capability, PortalWorldData instance, EnumFacing side, NBTBase nbt) {
				if (nbt instanceof NBTTagCompound) {
					instance.deserializeNBT((NBTTagCompound) nbt);
				}
			}

		}, PortalWorldData::new);
	}

	public static PortalWorldData getData(World world) {
		return world.getCapability(portalData, null);
	}

	public static List<PortalWorldData> getDatas() {
		return Arrays.stream(FMLCommonHandler.instance().getMinecraftServerInstance().worlds).map(PortalWorldData::getData).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static GlobalBlockPos getPosForName(String name) {
		return getDatas().stream().map(d -> GlobalBlockPos.fromTile(d.getTile(name))).filter(Objects::nonNull).findAny().orElse(null);
	}

	@SubscribeEvent
	public static void tick(WorldTickEvent event) {
		if (event.phase == Phase.END && !event.world.isRemote) {
			if (event.world.hasCapability(portalData, null))
				event.world.getCapability(portalData, null).update();
			for (int i = 0; i < event.world.loadedEntityList.size(); i++) {
				Entity e = event.world.loadedEntityList.get(i);
				if (TileController.portableEntity(e))
					if (e.getEntityData().getInteger("untilPort") > 0) {
						e.getEntityData().setInteger("untilPort", e.getEntityData().getInteger("untilPort") - 1);
					}
			}
		}
	}

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<World> event) {
		event.addCapability(LOCATION, new Provider((World) event.getObject()));
	}

	@SubscribeEvent
	public static void join(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (e instanceof EntityPlayerMP) {
			getData(event.getWorld()).sync((EntityPlayer) event.getEntity());
		}
		if (!event.getWorld().isRemote && TileController.portableEntity(e) && e.getEntityData().getBoolean("ported")) {
			e.getEntityData().setInteger("untilPort", TileController.untilPort);
		}
	}

	static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		PortalWorldData data = portalData.getDefaultInstance();

		public Provider(World world) {
			this.data.world = world;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == portalData;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? portalData.cast(data) : null;
		}

		@Override
		public NBTTagCompound serializeNBT() {
			return (NBTTagCompound) portalData.getStorage().writeNBT(portalData, data, null);
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt) {
			portalData.getStorage().readNBT(portalData, data, null, nbt);
		}

	}

}
