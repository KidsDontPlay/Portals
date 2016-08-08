package mrriegel.portals.tile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileFrame extends TileBase implements IPortalFrame {

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

	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {

	}

	public BlockPos getController() {
		return controller;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	@Override
	public TileController getTileController() {
		return controller != null && worldObj.getTileEntity(controller) instanceof TileController ? (TileController) worldObj.getTileEntity(controller) : null;
	}

}
