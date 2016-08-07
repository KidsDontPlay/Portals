package mrriegel.portals.blocks;

import mrriegel.portals.Portals;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileFrame;
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

public class BlockFrame extends BlockContainer {

	public BlockFrame() {
		super(Material.ROCK);
		setRegistryName("frame");
		setUnlocalizedName(getRegistryName().toString());
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileFrame();
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileFrame && ((TileFrame) tileentity).getController() != null) {
			BlockPos con = ((TileFrame) tileentity).getController();
			if (worldIn.getTileEntity(con) instanceof TileController)
				((TileController) worldIn.getTileEntity(con)).validatePortal();
			else
				((TileFrame) tileentity).setController(null);;
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.isRemote) {
			return true;
		} else {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof TileFrame && ((TileFrame) tileentity).getController() != null) {
				BlockPos con = ((TileFrame) tileentity).getController();
				if (worldIn.getTileEntity(con) instanceof TileController)
					playerIn.openGui(Portals.instance, 0, worldIn, con.getX(), con.getY(), con.getZ());
				else
					((TileFrame) tileentity).setController(null);
				return true;
			}
			return false;
		}
	}

}
