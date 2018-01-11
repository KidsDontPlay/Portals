package mrriegel.portals.tile;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.tile.CommonTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TilePortaal extends CommonTile {

	private BlockPos controller;
	private int color = 0xFFFFFF;

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		controller = NBTHelper.get(compound, "controller", BlockPos.class);
		color = NBTHelper.get(compound, "color", int.class);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTHelper.set(compound, "controller", controller);
		NBTHelper.set(compound, "color", color);
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

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
		markForSync();
	}

}
