package mrriegel.portals.proxy;

import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.IPortalFrame;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TilePortaal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModBlocks.initClient();
		ModItems.initClient();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) -> {
			if (worldIn == null || pos == null || ((TilePortaal) worldIn.getTileEntity(pos)).getController() == null || worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController()) == null || !((TileController) worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController())).getUpgrades().contains(Upgrade.COLOR))
				return 0xffffff;
			if (!((TileController) worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController())).isValid())
				return 0xffffff;
			return ((TileController) worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController())).getColorPortal();
		}, ModBlocks.portaal);
		IBlockColor frame = (IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) -> {
			if (worldIn == null || pos == null || !(worldIn.getTileEntity(pos) instanceof IPortalFrame) || ((IPortalFrame) worldIn.getTileEntity(pos)).getTileController() == null || !((IPortalFrame) worldIn.getTileEntity(pos)).getTileController().getUpgrades().contains(Upgrade.COLOR)) {
				return 0xffffff;
			}
			if (!((IPortalFrame) worldIn.getTileEntity(pos)).getTileController().isValid())
				return 0xffffff;
			return ((IPortalFrame) worldIn.getTileEntity(pos)).getTileController().getColorFrame();
		};
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(frame, ModBlocks.controller);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(frame, ModBlocks.frame);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}

}
