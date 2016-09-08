package mrriegel.portals.gui;

import java.io.IOException;

import mrriegel.limelib.gui.CommonGuiScreenSub;
import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

public class GuiUpgrade extends CommonGuiScreenSub {

	private static final ResourceLocation textures = new ResourceLocation(Portals.MODID + ":textures/gui/upgrade.png");

	TileController tile;
	Upgrade upgrade;

	public GuiUpgrade(TileController tile, Upgrade upgrade) {
		super();
		this.tile = tile;
		this.upgrade = upgrade;
		xSize = 101;
		ySize = 101;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE && parent != null) {
			Minecraft.getMinecraft().currentScreen = parent;
			onClosed();
		} else
			super.keyTyped(typedChar, keyCode);
	}

	protected NBTTagCompound getTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("id", upgrade.ordinal());
		return nbt;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawer.drawBackgroundTexture();
		mc.fontRendererObj.drawString(WordUtils.capitalize(upgrade.name().toLowerCase()), guiLeft + 8, guiTop + 6, 4210752);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
