package mrriegel.portals.gui;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.gui.CommonGuiScreenSub;
import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class GuiUpgrade extends CommonGuiScreenSub {

	private static final ResourceLocation textures = new ResourceLocation(Portals.MODID + ":textures/gui/upgrade.png");

	protected TileController tile;
	protected Upgrade upgrade;

	public GuiUpgrade(TileController tile, Upgrade upgrade) {
		super();
		this.tile = tile;
		this.upgrade = upgrade;
		xSize = 101;
		ySize = 101;
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
		mc.fontRenderer.drawString(WordUtils.capitalize(upgrade.name().toLowerCase()), guiLeft + 8, guiTop + 6, 4210752);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

}
