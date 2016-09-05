package mrriegel.portals.tile;

import mrriegel.limelib.tile.CommonTile;
import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileFrame extends CommonTile implements IPortalFrame {

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
		if (controller != null && !(worldObj.getTileEntity(controller) instanceof TileController))
			controller = null;
		return controller;
	}

	public void setController(BlockPos controller) {
		this.controller = controller;
	}

	@Override
	public TileController getTileController() {
		return getController() != null && worldObj.getTileEntity(getController()) instanceof TileController ? (TileController) worldObj.getTileEntity(getController()) : null;
	}

	@Override
	public boolean openGUI(EntityPlayerMP player) {
		if (getController() != null) {
			BlockPos con = getController();
			if (worldObj.getTileEntity(con) instanceof TileController)
				player.openGui(Portals.instance, GuiHandler.PORTAL, worldObj, con.getX(), con.getY(), con.getZ());
			else
				setController(null);
			return true;
		}
		return false;
	}

}
