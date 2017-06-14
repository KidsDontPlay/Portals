package mrriegel.portals.blocks;

import mrriegel.limelib.block.CommonBlockContainer;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.PortalData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockController extends CommonBlockContainer<TileController> {

	public BlockController() {
		super(Material.ROCK, "controller");
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileController();
	}

	@Override
	protected Class<? extends TileController> getTile() {
		return TileController.class;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos from) {
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
			PortalData data = PortalData.get(worldIn);
			data.remove(new GlobalBlockPos(pos, worldIn));
			for (GlobalBlockPos p : data.valids) {
				TileController t = (TileController) p.getTile();
				if (t != null && t.getTarget() != null && t.getTarget().equals(new GlobalBlockPos(pos, worldIn))) {
					t.setTarget(null);
				}
			}
		}
	}

}
