package mrriegel.portals.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;

public class TileCapa extends TileBasicFrame {

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		TileCapa tc = get(capability);
		if (tc != null)
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity t2 = tc.getWorld().getTileEntity(tc.getPos().offset(f.getOpposite()));
				boolean has = false;
				if (t2 != null && (has = t2.hasCapability(capability, f)))
					return has;
			}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		TileCapa tc = get(capability);
		if (tc != null)
			for (EnumFacing f : EnumFacing.VALUES) {
				TileEntity t2 = tc.getWorld().getTileEntity(tc.getPos().offset(f.getOpposite()));
				T get = null;
				if (t2 != null && (get = t2.getCapability(capability, f)) != null)
					return get;
			}
		return super.getCapability(capability, facing);
	}

	private TileCapa get(Capability<?> capability) {
		if (!world.isRemote && getController() != null && world.getTileEntity(getController()) instanceof TileController) {
			TileController that = (TileController) world.getTileEntity(getController());
			if (that.getTarget() != null && DimensionManager.getWorld(that.getTarget().getDimension()) != null && that.getTarget().getWorld().isBlockLoaded(that.getTarget().getPos()) && that.getTarget().getTile() != null) {
				TileController tcon = (TileController) that.getTarget().getTile();
				TileCapa tc = (TileCapa) tcon.getFrames().stream().map(p -> tcon.getWorld().getTileEntity(p)).filter(t -> t instanceof TileCapa).findAny().orElse(null);
				if (tc != null)
					return tc;
			}
		}
		return null;
	}

}
