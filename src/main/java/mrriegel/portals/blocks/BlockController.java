package mrriegel.portals.blocks;

import mrriegel.limelib.block.CommonBlockContainer;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockController extends CommonBlockContainer<TileController> {

	public BlockController() {
		super(Material.ROCK, "controller");
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	protected Class<? extends TileController> getTile() {
		return TileController.class;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			TileController tile = (TileController) worldIn.getTileEntity(pos);
			tile.validatePortal();
			if (worldIn.isBlockPowered(pos) && !tile.isActive())
				tile.activate();
			else if (!worldIn.isBlockPowered(pos) && tile.isActive())
				tile.deactivate();
		}
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			((TileController) worldIn.getTileEntity(pos)).validatePortal();
		}
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		// if (!worldIn.isRemote)
		{
			PortalWorldData data = PortalWorldData.getData(worldIn);
			data.remove(pos);
			data.validControllers.stream().map(p -> (TileController) worldIn.getTileEntity(p)).//
					filter(t -> t != null && new GlobalBlockPos(pos, worldIn).equals(t.getTarget())).forEach(t -> t.setTarget(null));
		}
	}

}
