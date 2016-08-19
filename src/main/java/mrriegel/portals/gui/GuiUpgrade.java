package mrriegel.portals.gui;

import java.io.IOException;

import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

public class GuiUpgrade extends GuiScreen {

	private static final ResourceLocation textures = new ResourceLocation(Portals.MODID + ":textures/gui/upgrade.png");

	GuiPortal parent;
	int imageWidth = 101;
	int imageHeight = 101;
	int guiLeft, guiTop;
	TileController tile;
	Upgrade upgrade;

	public GuiUpgrade(GuiPortal parent, TileController tile, Upgrade upgrade) {
		this.parent = parent;
		this.tile = tile;
		this.upgrade = upgrade;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE && parent != null) {
			Minecraft.getMinecraft().currentScreen = parent;
		} else
			super.keyTyped(typedChar, keyCode);
	}
	@Override
	public void initGui() {
		super.initGui();
		guiLeft = (this.width - this.imageWidth) / 2;
		guiTop = (this.height - this.imageHeight) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(textures);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
		super.drawScreen(mouseX, mouseY, partialTicks);
		mc.fontRendererObj.drawString(WordUtils.capitalize(upgrade.name().toLowerCase()), guiLeft + 8, guiTop + 6, 4210752);
	}
	
	protected NBTTagCompound getTag(){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("id", upgrade.ordinal());
		nbt.setLong("pos", tile.getPos().toLong());
		return nbt;
	}

}
