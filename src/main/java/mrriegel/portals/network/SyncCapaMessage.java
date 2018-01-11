package mrriegel.portals.network;

import mrriegel.limelib.network.AbstractMessage;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public class SyncCapaMessage extends AbstractMessage {

	public SyncCapaMessage() {
	}

	public SyncCapaMessage(PortalWorldData data) {
		nbt = data.serializeNBT();
		//		Map<String, NBTTagCompound> map = PortalWorldData.totalControllers.stream().map(gp -> (TileController) gp.getTile()).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		//PortalWorldData.getDatas().stream().flatMap(d -> d.validControllers.stream().map(p -> (TileController) d.world.getTileEntity(p))).collect(Collectors.toMap(TileController::getName, t -> GlobalBlockPos.fromTile(t).writeToNBT(new NBTTagCompound())));
		//		NBTHelper.setMap(nbt, "map", map);

	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		PortalWorldData.INSTANCE.deserializeNBT(nbt);
		//		Map<String, NBTTagCompound> map = NBTHelper.getMap(nbt, "map", String.class, NBTTagCompound.class);
		//		Map<String, GlobalBlockPos> map2 = new HashMap<>();
		//		map.forEach((s, n) -> map2.put(s, GlobalBlockPos.loadGlobalPosFromNBT(n)));
		//		PortalWorldData.INSTANCE.posMap = map2;
		//		System.out.println(map2);
	}

}
