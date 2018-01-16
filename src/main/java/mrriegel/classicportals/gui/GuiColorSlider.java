package mrriegel.classicportals.gui;

import java.awt.Color;
import java.io.IOException;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.input.Keyboard;

import mrriegel.limelib.gui.CommonGuiScreen;
import mrriegel.limelib.gui.GuiDrawer;
import mrriegel.limelib.helper.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiSlider;

public class GuiColorSlider extends CommonGuiScreen {
	GuiUpgrade parent;
	MutableInt i;

	public GuiColorSlider(GuiUpgrade parent, MutableInt i) {
		super();
		this.parent = parent;
		this.i = i;
		xSize = 100;
		ySize = 130;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiSlider(0, 7 + guiLeft, 7 + guiTop, 86, 20, "Red ", "", 0, 255, ColorHelper.getRed(i.intValue()), false, true));
		buttonList.add(new GuiSlider(1, 7 + guiLeft, 32 + guiTop, 86, 20, "Green ", "", 0, 255, ColorHelper.getGreen(i.intValue()), false, true));
		buttonList.add(new GuiSlider(2, 7 + guiLeft, 57 + guiTop, 86, 20, "Blue ", "", 0, 255, ColorHelper.getBlue(i.intValue()), false, true));
		//		buttonList.add(new GuiSlider(3, 7 + guiLeft, 83 + guiTop, 86, 20, "Alpha ", "", 0, 255, ColorHelper.getAlpha(i.intValue()), false, true));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		drawer.drawBackgroundTexture();
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawer.drawColoredRectangle(7, 109, 86, 12, i.intValue());
		drawer.drawFrame(7, 108, 86, 13, 1, Color.darkGray.getRGB());
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			GuiDrawer.openGui(parent);
		} else
			super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		int red = (int) MathHelper.clamp(((GuiSlider) buttonList.get(0)).sliderValue * 255, 0, 255);
		int green = (int) MathHelper.clamp(((GuiSlider) buttonList.get(1)).sliderValue * 255, 0, 255);
		int blue = (int) MathHelper.clamp(((GuiSlider) buttonList.get(2)).sliderValue * 255, 0, 255);
		//		int alpha = (int) MathHelper.clamp(((GuiSlider) buttonList.get(3)).sliderValue * 255, 0, 255);
		int color = new Color(red, green, blue, 255).getRGB();
		i.setValue(color);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

}