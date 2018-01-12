package mrriegel.portals.blocks;

import mrriegel.limelib.block.CommonBlockContainer;
import mrriegel.portals.tile.TileController;
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
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			((TileController) worldIn.getTileEntity(pos)).validatePortal();
		}
	}

}
