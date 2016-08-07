package mrriegel.portals.blocks;

import mrriegel.portals.Portals;
import mrriegel.portals.tile.TileController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockController extends BlockContainer {

	public BlockController() {
		super(Material.ROCK);
		setRegistryName("controller");
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileController();
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			((TileController) worldIn.getTileEntity(pos)).validatePortal();
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			((TileController) worldIn.getTileEntity(pos)).validatePortal();
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileController) {
				TileController tile = (TileController) worldIn.getTileEntity(pos);
				if (playerIn.getHeldItemMainhand() != null) {
					if (playerIn.getHeldItemMainhand().getItem() == Items.STICK) {
						if (!tile.isActive())
							tile.activate(playerIn);
						else
							tile.deactivate();
					}else if (playerIn.getHeldItemMainhand().getItem() == Items.WHEAT_SEEDS) {
						System.out.println("active: "+tile.isActive());
					}
				}
				playerIn.openGui(Portals.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
	}
}
