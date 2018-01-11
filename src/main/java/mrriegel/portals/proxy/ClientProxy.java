package mrriegel.portals.proxy;

import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.tile.TileBasicFrame;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TilePortaal;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
			if (worldIn == null || pos == null || !(worldIn.getTileEntity(pos) instanceof TilePortaal))
				return 0xffffff;
			return PortalWorldData.INSTANCE.portalColors.getInt(GlobalBlockPos.fromTile(worldIn.getTileEntity(pos)));
		}, ModBlocks.portaal);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) -> {
			if (worldIn == null || pos == null || !(worldIn.getTileEntity(pos) instanceof TileBasicFrame))
				return 0xffffff;
			return PortalWorldData.INSTANCE.frameColors.getInt(GlobalBlockPos.fromTile(worldIn.getTileEntity(pos)));
		}, ModBlocks.frame, ModBlocks.capa);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) -> {
			if (worldIn == null || pos == null || !(worldIn.getTileEntity(pos) instanceof TileController))
				return 0xffffff;
			return ((TileController) worldIn.getTileEntity(pos)).getColorFrame();
		}, ModBlocks.controller);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}

}
