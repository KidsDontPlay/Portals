package mrriegel.portals.blocks;

import mrriegel.limelib.block.CommonBlockContainer;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileFrame;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFrame extends CommonBlockContainer<TileFrame> {

	public BlockFrame() {
		super(Material.ROCK, "frame");
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public TileEntity createTileEntity(World worldIn, IBlockState state) {
		return new TileFrame();
	}

	@Override
	protected Class<? extends TileFrame> getTile() {
		return TileFrame.class;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos from) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileFrame && ((TileFrame) tileentity).getController() != null) {
			BlockPos con = ((TileFrame) tileentity).getController();
			if (worldIn.getTileEntity(con) instanceof TileController)
				((TileController) worldIn.getTileEntity(con)).validatePortal();
			else
				((TileFrame) tileentity).setController(null);
		}
	}

}
