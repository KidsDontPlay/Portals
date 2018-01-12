package mrriegel.portals.tile;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.tile.CommonTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class TileBasicFrame extends CommonTile {

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
		markDirty();
	}

	@Override
	public boolean openGUI(EntityPlayerMP player) {
		BlockPos con = getController();
		if (con != null) {
			if (world.getTileEntity(con) instanceof TileController)
				return ((TileController) world.getTileEntity(con)).openGUI(player);
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, Block block, BlockPos fromPos) {
		if (getController() != null) {
			if (world.getTileEntity(getController()) instanceof TileController) {
				((TileController) world.getTileEntity(getController())).validatePortal();
			} else
				setController(null);
		}
	}

}
