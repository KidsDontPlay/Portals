package mrriegel.portals.network;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
		nbt = data.serializeNBT();
		Map<String, NBTTagCompound> map = PortalWorldData.getDatas().stream().flatMap(d -> d.validControllers.stream().map(p -> (TileController) d.world.getTileEntity(p))).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		NBTHelper.setMap(nbt, "map", map);
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		PortalWorldData.getData(player.world).deserializeNBT(nbt);
		Map<String, NBTTagCompound> map = NBTHelper.getMap(nbt, "map", String.class, NBTTagCompound.class);
		Map<String, GlobalBlockPos> map2 = new HashMap<>();
		map.forEach((s, n) -> map2.put(s, GlobalBlockPos.loadGlobalPosFromNBT(n)));
		PortalWorldData.getData(player.world).posMap = map2;
	}

}
