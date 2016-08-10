package mrriegel.portals.gui;

import mrriegel.portals.PortalData;
import mrriegel.portals.network.MessageData;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int PORTAL = 1000;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (player instanceof EntityPlayerMP)
			PacketHandler.INSTANCE.sendTo(new MessageData(PortalData.get(world).valids), (EntityPlayerMP) player);
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
		return null;
	}

}
