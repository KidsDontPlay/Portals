package mrriegel.portals.gui;

import java.awt.Color;

import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageUpgrade;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiUpgradeColor extends GuiUpgrade {

	GuiSlider portal, frame;

	public GuiUpgradeColor(GuiPortal parent, TileController tile, Upgrade upgrade) {
		super(parent, tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		Color p = new Color(tile.getColorPortal());
		buttonList.add(portal = new GuiSlider(0, guiLeft + 10, guiTop + 30, 80, 20, "val ", " %", 0, 1, Color.RGBtoHSB(p.getRed(), p.getGreen(), p.getBlue(), null)[0], false, false));
		Color f = new Color(tile.getColorFrame());
		buttonList.add(frame = new GuiSlider(1, guiLeft + 10, guiTop + 64, 80, 20, "val ", " %", 0, 1, Color.RGBtoHSB(f.getRed(), f.getGreen(), f.getBlue(), null)[0], false, false));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		fontRendererObj.drawString("Portal:", guiLeft + 15, guiTop + 20, Color.HSBtoRGB((float) portal.getValue(), .9f, 1));
		fontRendererObj.drawString("Frame:", guiLeft + 15, guiTop + 54, Color.HSBtoRGB((float) frame.getValue(), .9f, 1));
		// drawRect(guiLeft + 12, guiTop + 18, guiLeft + 12+15, guiTop + 18+15,
		// slider.getValueInt());
	}

	@Override
	protected void onClosed() {
		NBTTagCompound nbt = getTag();
		nbt.setInteger("colorP", Color.HSBtoRGB((float) portal.getValue(), .7f, 1));
		nbt.setInteger("colorF", Color.HSBtoRGB((float) frame.getValue(), .7f, 1));
		tile.setColorPortal(Color.HSBtoRGB((float) portal.getValue(), .7f, 1));
		tile.setColorFrame(Color.HSBtoRGB((float) frame.getValue(), .7f, 1));
		for (BlockPos pos : tile.getPortals())
			tile.getWorld().markBlockRangeForRenderUpdate(pos, pos);
		PacketHandler.INSTANCE.sendToServer(new MessageUpgrade(nbt));
	}

}
