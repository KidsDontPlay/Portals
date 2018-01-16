package mrriegel.classicportals.blocks;

import mrriegel.classicportals.tile.TileController;
import mrriegel.limelib.block.CommonBlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		if (worldIn.getTileEntity(pos) instanceof TileController && placer instanceof EntityPlayer) {
			((TileController) worldIn.getTileEntity(pos)).setOwner(placer.getName());
		}
	}

}
