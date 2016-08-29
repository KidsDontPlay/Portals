package mrriegel.portals.gui;

import java.awt.Color;

import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageUpgrade;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiUpgradeParticle extends GuiUpgrade {

	GuiSlider color;

	public GuiUpgradeParticle(GuiPortal parent, TileController tile, Upgrade upgrade) {
		super(parent, tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		Color p = new Color(tile.getColorParticle());
		buttonList.add(color = new GuiSlider(0, guiLeft + 10, guiTop + 30, 80, 20, "val ", " %", 0, 1, Color.RGBtoHSB(p.getRed(), p.getGreen(), p.getBlue(), null)[0], false, false));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		fontRendererObj.drawString("Color:", guiLeft + 15, guiTop + 20, Color.HSBtoRGB((float) color.getValue(), .9f, 1));
	}

	@Override
	protected void onClosed() {
		NBTTagCompound nbt = getTag();
		nbt.setInteger("color", Color.HSBtoRGB((float) color.getValue(), .7f, 1));
		tile.setColorPortal(Color.HSBtoRGB((float) color.getValue(), .7f, 1));
		PacketHandler.INSTANCE.sendToServer(new MessageUpgrade(nbt));
	}

}
