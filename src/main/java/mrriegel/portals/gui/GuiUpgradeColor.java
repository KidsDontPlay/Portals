package mrriegel.portals.gui;

import java.awt.Color;
import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiSlider;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageUpgrade;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;

public class GuiUpgradeColor extends GuiUpgrade {

	GuiSlider portal,frame;

	public GuiUpgradeColor(GuiPortal parent, TileController tile, Upgrade upgrade) {
		super(parent, tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		Color p=new Color(tile.getColorPortal());
		buttonList.add(portal = new GuiSlider(0, guiLeft + 15, guiTop + 35, 70, 17, "val ", " %", 0, 1, Color.RGBtoHSB(p.getRed(), p.getGreen(), p.getBlue(), null)[0], false, false));
		Color f=new Color(tile.getColorFrame());
		buttonList.add(frame = new GuiSlider(1, guiLeft + 15, guiTop + 60, 70, 17, "val ", " %", 0, 1, Color.RGBtoHSB(f.getRed(), f.getGreen(), f.getBlue(), null)[0], false, false));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		fontRendererObj.drawString("Portal", guiLeft + 12, guiTop + 18, Color.HSBtoRGB((float) portal.getValue(), 1, 1));
		fontRendererObj.drawString("Frame", guiLeft + 48, guiTop + 18, Color.HSBtoRGB((float) frame.getValue(), 1, 1));
		// drawRect(guiLeft + 12, guiTop + 18, guiLeft + 12+15, guiTop + 18+15,
		// slider.getValueInt());
	}

	@Override
	protected void onClosed() {
		NBTTagCompound nbt = getTag();
		nbt.setInteger("colorP", Color.HSBtoRGB((float) portal.getValue(), 1, 1));
		nbt.setInteger("colorF", Color.HSBtoRGB((float) frame.getValue(), 1, 1));
		PacketHandler.INSTANCE.sendToServer(new MessageUpgrade(nbt));
	}

}
