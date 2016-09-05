package mrriegel.portals.tile;

import mrriegel.limelib.tile.CommonTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TilePortaal extends CommonTile {

	private BlockPos controller;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("controller"))
			controller = BlockPos.fromLong(compound.getLong("controller"));
		else
			controller = null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (controller != null)
			compound.setLong("controller", controller.toLong());
		return super.writeToNBT(compound);
	}

	public BlockPos getController() {
		return controller;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

}
