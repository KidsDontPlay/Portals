package mrriegel.portals.gui;

import java.awt.Color;

import com.google.common.base.Strings;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiUpgradeColor extends GuiUpgrade {

	GuiSlider portal, frame;

	public GuiUpgradeColor(TileController tile, Upgrade upgrade) {
		super(tile, upgrade);
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
		fontRenderer.drawString("Portal: " + Strings.repeat("\u25A0", 6), guiLeft + 15, guiTop + 20, Color.HSBtoRGB((float) portal.getValue(), .9f, 1f));
		fontRenderer.drawString("Frame: " + Strings.repeat("\u25A0", 6), guiLeft + 15, guiTop + 54, Color.HSBtoRGB((float) frame.getValue(), .9f, 1f));
		// drawRect(guiLeft + 12, guiTop + 18, guiLeft + 12+15, guiTop + 18+15,
		// slider.getValueInt());
	}

	@Override
	protected void onClosed() {
		super.onClosed();
		NBTTagCompound nbt = getTag();
		NBTHelper.set(nbt, "colorP", Color.HSBtoRGB((float) portal.getValue(), .7f, 1));
		NBTHelper.set(nbt, "colorF", Color.HSBtoRGB((float) frame.getValue(), .7f, 1));
		NBTHelper.set(nbt, "kind", TileController.UPGRADE);
		tile.handleMessage(mc.player, nbt);
		tile.sendMessage(nbt);
		for (BlockPos p : tile.getFrames())
			mc.world.markBlockRangeForRenderUpdate(p.add(-1, -1, -1), p.add(1, 1, 1));
		for (BlockPos p : tile.getPortals())
			mc.world.markBlockRangeForRenderUpdate(p.add(-1, -1, -1), p.add(1, 1, 1));
	}

}
