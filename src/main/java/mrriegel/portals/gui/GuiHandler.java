package mrriegel.portals.gui;

import mrriegel.limelib.tile.CommonTile;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int PORTAL = 1000;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (world.getTileEntity(new BlockPos(x, y, z)) instanceof CommonTile)
			((CommonTile) world.getTileEntity(new BlockPos(x, y, z))).sync();
		if (ID == PORTAL) {
			return new ContainerPortal((TileController) world.getTileEntity(new BlockPos(x, y, z)), player.inventory);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == PORTAL) {
			return new GuiPortal(new ContainerPortal((TileController) world.getTileEntity(new BlockPos(x, y, z)), player.inventory));
		}
		// if (Minecraft.getMinecraft().currentScreen instanceof GuiPortal)
		// return new GuiUpgrade((GuiPortal)
		// Minecraft.getMinecraft().currentScreen);
		return null;
	}

}
