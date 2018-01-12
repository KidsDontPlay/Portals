package mrriegel.portals.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.Utils;
import mrriegel.portals.Portals;
import mrriegel.portals.network.SyncCapaMessage;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@EventBusSubscriber(modid = Portals.MODID)
public class PortalWorldData implements INBTSerializable<NBTTagCompound> {

	public static final PortalWorldData INSTANCE = new PortalWorldData();

	public Set<GlobalBlockPos> validControllers = new HashSet<>();
	private File cons;
	//client
	public BiMap<GlobalBlockPos, String> names = HashBiMap.create();
	public Object2IntMap<GlobalBlockPos> frameColors = new Object2IntOpenHashMap<>();
	public Object2IntMap<GlobalBlockPos> portalColors = new Object2IntOpenHashMap<>();

	private PortalWorldData() {
		frameColors.defaultReturnValue(0xFFFFFF);
		portalColors.defaultReturnValue(0xFFFFFF);
	}

	public void add(GlobalBlockPos pos) {
		if (!validControllers.contains(pos)) {
			validControllers.add(pos);
			sync(null);
		}
	}

	public void remove(GlobalBlockPos pos) {
		if (validControllers.contains(pos)) {
			validControllers.remove(pos);
			sync(null);
		}
	}

	public void sync(EntityPlayer player) {
		for (EntityPlayer p : player != null ? Collections.singletonList(player) : Arrays.stream(FMLCommonHandler.instance().getMinecraftServerInstance().worlds).flatMap(w -> w.playerEntities.stream()).collect(Collectors.toList())) {
			PacketHandler.sendTo(new SyncCapaMessage(this), (EntityPlayerMP) p);
		}
	}

	public boolean validPos(GlobalBlockPos pos) {
		return validControllers.contains(pos);
	}

	public List<String> getNames() {
		validate();
		return validControllers.stream().filter(this::validPos).map(p -> ((TileController) p.getTile()).getName()).collect(Collectors.toList());
	}

	public TileController getTile(String name) {
		validate();
		return validControllers.stream().filter(p -> validPos(p) && ((TileController) p.getTile()).getName().equals(name)).map(p -> ((TileController) p.getTile())).findAny().orElse(null);
	}

	public boolean nameOccupied(String name, GlobalBlockPos pos) {
		validate();
		return validControllers.stream().filter(p -> validPos(p) && !pos.equals(p)).map(p -> ((TileController) p.getTile())).anyMatch(t -> t.getName().equals(name));
	}

	public void validate() {
		validControllers.removeIf(p -> {
			TileEntity t = p.getTile();
			if (!(t instanceof TileController))
				return true;
			return !((TileController) t).isValid();
		});
	}

	public void refreshColors() {
		frameColors.clear();
		portalColors.clear();
		List<TileController> lis = validControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toList());
		for (TileController t : lis) {
			for (BlockPos p : t.getFrames())
				frameColors.put(new GlobalBlockPos(p, t.getWorld()), t.getColorFrame());
			for (BlockPos p : t.getPortals())
				portalColors.put(new GlobalBlockPos(p, t.getWorld()), t.getColorPortal());
		}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTHelper.setList(nbt, "controllers", validControllers.stream().map(p -> p.writeToNBT(new NBTTagCompound())).collect(Collectors.toList()));
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		validControllers = NBTHelper.getList(nbt, "controllers", NBTTagCompound.class).stream().map(GlobalBlockPos::loadGlobalPosFromNBT).collect(Collectors.toSet());
	}

	@SubscribeEvent
	public static void tick(WorldTickEvent event) {
		if (event.phase == Phase.END && !event.world.isRemote) {
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
	public static void join(EntityJoinWorldEvent event) {
		Entity e = event.getEntity();
		if (e instanceof EntityPlayerMP) {
			INSTANCE.sync((EntityPlayer) event.getEntity());
		}
		if (!event.getWorld().isRemote && TileController.portableEntity(e) && e.getEntityData().getBoolean("ported")) {
			e.getEntityData().setInteger("untilPort", TileController.untilPort);
		}
	}

	public static void start(MinecraftServer server) throws IOException {
		File dir = DimensionManager.getCurrentSaveRootDirectory();
		if (dir == null)
			dir = server.getActiveAnvilConverter().getSaveLoader(server.getFolderName(), false).getWorldDirectory();
		dir.mkdirs();
		File myDir = new File(dir, "Portal_Data");
		myDir.mkdirs();
		INSTANCE.cons = new File(myDir, "portals.json");
		BufferedReader br = new BufferedReader(new FileReader(INSTANCE.cons));
		@SuppressWarnings("serial")
		NBTTagCompound n = Utils.getGSON().fromJson(br, new TypeToken<NBTTagCompound>() {
		}.getType());
		br.close();
		if (n != null) {
			INSTANCE.deserializeNBT(n);
		}
	}

	public static void stop() throws IOException {
		String s = Utils.getGSON().toJson(INSTANCE.serializeNBT());
		BufferedWriter bw = new BufferedWriter(new FileWriter(INSTANCE.cons));
		bw.write(s);
		bw.close();
	}

}
