package mrriegel.portals.proxy;

import java.util.Map;

import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.tile.IPortalFrame;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TilePortaal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.controller), 0, new ModelResourceLocation(ModBlocks.controller.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.frame), 0, new ModelResourceLocation(ModBlocks.frame.getRegistryName(), "inventory"));
		// ModelLoader.setCustomStateMapper(ModBlocks.frame, new IStateMapper()
		// {
		// @Override
		// public Map<IBlockState, ModelResourceLocation>
		// putStateModelLocations(Block blockIn) {
		// return Maps.newHashMap();
		// }
		// });
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		// MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess worldIn, BlockPos pos, int tintIndex) {
				if (worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController()) == null)
					return 0;
				return ((TileController) worldIn.getTileEntity(((TilePortaal) worldIn.getTileEntity(pos)).getController())).getColorPortal();
			}
		}, ModBlocks.portaal);
	}

	@SubscribeEvent
	public void render(RenderWorldLastEvent event) {
		for (TileEntity tile : Minecraft.getMinecraft().theWorld.loadedTileEntityList) {
			if (tile instanceof IPortalFrame) {
				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
				World world = Minecraft.getMinecraft().theWorld;
				BlockPos blockpos = tile.getPos();
				IBlockState iblockstate = Blocks.EMERALD_BLOCK.getDefaultState();
				double xx = (double) blockpos.getX() - TileEntityRendererDispatcher.staticPlayerX;
				double yy = (double) blockpos.getY() - TileEntityRendererDispatcher.staticPlayerY;
				double zz = (double) blockpos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;

				GlStateManager.pushMatrix();
				RenderHelper.disableStandardItemLighting();
				GlStateManager.translate((float) xx, (float) yy, (float) zz);

				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer worldrenderer = tessellator.getBuffer();
				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				int i = blockpos.getX();
				int j = blockpos.getY();
				int k = blockpos.getZ();

				worldrenderer.setTranslation(((-i)), (-j), ((-k)));
				worldrenderer.color(1F, 1F, 1F, 1F);
				IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate);
				blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, true);

				worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
				tessellator.draw();
				RenderHelper.enableStandardItemLighting();
				GlStateManager.popMatrix();
			}
		}
	}

}
