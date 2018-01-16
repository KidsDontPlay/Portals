package mrriegel.classicportals.network;

import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import mrriegel.classicportals.tile.TileController;
import mrriegel.classicportals.util.PortalWorldData;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.AbstractMessage;
import mrriegel.limelib.util.GlobalBlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class SyncCapaMessage extends AbstractMessage {

	public SyncCapaMessage() {
	}

	public SyncCapaMessage(PortalWorldData data) {
		data.validate();
		nbt = data.serializeNBT();
		Map<String, NBTTagCompound> map = data.validControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		NBTHelper.setMap(nbt, "map", map);
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		PortalWorldData.INSTANCE.deserializeNBT(nbt);
		Map<String, NBTTagCompound> map = NBTHelper.getMap(nbt, "map", String.class, NBTTagCompound.class);
		BiMap<GlobalBlockPos, String> map2 = HashBiMap.create();
		map.forEach((s, n) -> map2.put(GlobalBlockPos.loadGlobalPosFromNBT(n), s));
		PortalWorldData.INSTANCE.names = map2;
		PortalWorldData.INSTANCE.refreshColors();
	}

}
