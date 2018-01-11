package mrriegel.portals.network;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.AbstractMessage;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class SyncCapaMessage extends AbstractMessage {

	public SyncCapaMessage() {
	}

	public SyncCapaMessage(PortalWorldData data) {
		data.validate();
		nbt = data.serializeNBT();
		//		NBTHelper.setMap(nbt, "names", new ArrayList<>(PortalWorldData.INSTANCE.getNames()));
		Map<String, NBTTagCompound> map = data.validControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		//		PortalWorldData.I.stream().flatMap(d -> d.validControllers.stream().map(p -> (TileController) d.world.getTileEntity(p))).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		NBTHelper.setMap(nbt, "map", map);
		//		System.out.println("in1: " + NBTHelper.getSize(nbt));
		//		Map<NBTTagCompound, Integer> fcolors = data.validControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toMap(t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound()), TileController::getColorFrame));
		//		NBTHelper.setMap(nbt, "fcolors", fcolors);
		//		Map<NBTTagCompound, Integer> pcolors = data.validControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toMap(t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound()), TileController::getColorPortal));
		//		NBTHelper.setMap(nbt, "pcolors", pcolors);
		//		System.out.println("in2: " + NBTHelper.getSize(nbt));
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		PortalWorldData.INSTANCE.deserializeNBT(nbt);
		//		PortalWorldData.INSTANCE.names = new HashSet<>(NBTHelper.getList(nbt, "names", String.class));
		Map<String, NBTTagCompound> map = NBTHelper.getMap(nbt, "map", String.class, NBTTagCompound.class);
		BiMap<String, GlobalBlockPos> map2 = HashBiMap.create();
		map.forEach((s, n) -> map2.put(s, GlobalBlockPos.loadGlobalPosFromNBT(n)));
		PortalWorldData.INSTANCE.names = map2.inverse();
		PortalWorldData.INSTANCE.refreshColors();
		//		Map<NBTTagCompound, Integer> fcolors = NBTHelper.getMap(nbt, "fcolors", NBTTagCompound.class, int.class);
		//		PortalWorldData.INSTANCE.frameColors.clear();
		//		for (Map.Entry<NBTTagCompound, Integer> e : fcolors.entrySet())
		//			PortalWorldData.INSTANCE.frameColors.put(GlobalBlockPos.loadGlobalPosFromNBT(e.getKey()), e.getValue());
		//		Map<NBTTagCompound, Integer> pcolors = NBTHelper.getMap(nbt, "pcolors", NBTTagCompound.class, int.class);
		//		PortalWorldData.INSTANCE.portalColors.clear();
		//		for (Map.Entry<NBTTagCompound, Integer> e : pcolors.entrySet())
		//			PortalWorldData.INSTANCE.portalColors.put(GlobalBlockPos.loadGlobalPosFromNBT(e.getKey()), e.getValue());
	}

}
