package mrriegel.classicportals.blocks;

import mrriegel.classicportals.tile.TileBasicFrame;
import mrriegel.limelib.block.CommonBlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockBasicFrame extends CommonBlockContainer<TileBasicFrame> {

	public BlockBasicFrame(String name) {
		super(Material.ROCK, name);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		setHardness(2.5f);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileBasicFrame) {
			((TileBasicFrame) tileentity).neighborChanged(state, null, null);
		}
		super.breakBlock(worldIn, pos, state);
	}

}
