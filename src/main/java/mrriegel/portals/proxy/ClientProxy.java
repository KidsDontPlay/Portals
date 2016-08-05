package mrriegel.portals.proxy;

import mrriegel.portals.init.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.controller), 0, new ModelResourceLocation(ModBlocks.controller.getRegistryName(), "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.frame), 0, new ModelResourceLocation(ModBlocks.frame.getRegistryName(), "inventory"));
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((IBlockColor) ModBlocks.portaal, ModBlocks.portaal);
	}

}
