package mrriegel.portals.blocks;

import mrriegel.portals.Portals;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.GlobalBlockPos;
import mrriegel.portals.util.PortalData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
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
		if (worldIn.getTileEntity(pos) instanceof TileController) {
			for (ItemStack stack : ((TileController) worldIn.getTileEntity(pos)).getStacks()) {
				if (stack != null)
					spawnAsEntity(worldIn, pos, stack);
			}
		}
		super.breakBlock(worldIn, pos, state);
		// if (!worldIn.isRemote)
		{
			PortalData data = PortalData.get(worldIn);
			data.remove(new GlobalBlockPos(pos, worldIn));
			for (GlobalBlockPos p : data.valids) {
				TileController t = (TileController) p.getTile(worldIn);
				if (t != null && t.getTarget() != null && t.getTarget().equals(new GlobalBlockPos(pos, worldIn))) {
					t.setTarget(null);
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileController) {
				// TileController tile = (TileController)
				// worldIn.getTileEntity(pos);
				playerIn.openGui(Portals.instance, GuiHandler.PORTAL, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
	}
}
