package mrriegel.classicportals.tile;

import mrriegel.classicportals.ClassicPortals;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

public class TileCapa extends TileBasicFrame {

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		TileCapa tc = get();
		if (tc != null)
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity t2 = tc.getWorld().getTileEntity(tc.getPos().offset(f.getOpposite()));
				boolean has = false;
				if (t2 != null && !t2.getPos().equals(getController()) && t2.getWorld().isBlockLoaded(t2.getPos()) && (has = t2.hasCapability(capability, facing == null ? null : f)))
					return has;
			}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		TileCapa tc = get();
		if (tc != null)
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity t2 = tc.getWorld().getTileEntity(tc.getPos().offset(f.getOpposite()));
				T get = null;
				if (t2 != null && !t2.getPos().equals(getController()) && t2.getWorld().isBlockLoaded(t2.getPos()) && (get = t2.getCapability(capability, facing == null ? null : f)) != null)
					return get;

			}
		return super.getCapability(capability, facing);
	}

	private TileCapa get() {
		if (!world.isRemote && getController() != null && world.getTileEntity(getController()) instanceof TileController) {
			TileController that = (TileController) world.getTileEntity(getController());
			if (that.getTarget() != null && that.isActive() && DimensionManager.getWorld(that.getTarget().getDimension()) != null && that.getTarget().getWorld().isBlockLoaded(that.getTarget().getPos()) && that.getTarget().getTile() != null) {
				TileController tcon = (TileController) that.getTarget().getTile();
				TileCapa tc = (TileCapa) tcon.getFrames().stream().map(p -> tcon.getWorld().getTileEntity(p)).filter(t -> t instanceof TileCapa).findAny().orElse(null);
				if (tc != null)
					return tc;
			}
		}
		return null;
	}

	@Override
	public void neighborChanged(IBlockState state, Block block, BlockPos fromPos) {
		super.neighborChanged(state, block, fromPos);
		TileCapa tc = get();
		if (tc != null) {
			for (EnumFacing f : EnumFacing.VALUES) {
				BlockPos np = tc.getPos().offset(f);
				if (tc.world.getBlockState(np).getBlock().getRegistryName().getResourceDomain().equals(ClassicPortals.MODID))
					continue;
				else {
					tc.world.getBlockState(np).neighborChanged(tc.world, np, tc.getBlockType(), tc.pos);
				}
			}
		}
	}

}
