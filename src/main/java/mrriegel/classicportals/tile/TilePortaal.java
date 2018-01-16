package mrriegel.classicportals.tile;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.tile.CommonTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TilePortaal extends CommonTile {

	private BlockPos controller;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		controller = NBTHelper.get(compound, "controller", BlockPos.class);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTHelper.set(compound, "controller", controller);
		return super.writeToNBT(compound);
	}

	public BlockPos getController() {
		if (controller != null && !(world.getTileEntity(controller) instanceof TileController))
			controller = null;
		return controller;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

}
