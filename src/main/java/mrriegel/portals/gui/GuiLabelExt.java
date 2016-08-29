package mrriegel.portals.gui;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import com.google.common.collect.Lists;

public class GuiLabelExt extends GuiLabel {

	protected boolean hovered;
	protected int width = 200;
	protected int height = 20;
	public int x;
	public int y;
	public final List<String> labels;
	public int id;
	private boolean centered;
	public boolean visible = true;
	private boolean labelBgEnabled;
	protected int textColor;
	private int backColor;
	private int ulColor;
	private int brColor;
	private final FontRenderer fontRenderer;
	private int border;

	public GuiLabelExt(FontRenderer fontRendererObj, int id, int x, int y, int width, int height, int color) {
		super(fontRendererObj, id, x, y, width, height, color);
		this.fontRenderer = fontRendererObj;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.labels = Lists.<String> newArrayList();
		this.centered = false;
		this.labelBgEnabled = false;
		this.textColor = color;
		this.backColor = -1;
		this.ulColor = -1;
		this.brColor = -1;
		this.border = 0;
	}

	@Override
	public void addLine(String p_175202_1_) {
		this.labels.add(I18n.format(p_175202_1_, new Object[0]));
	}

	@Override
	public GuiLabel setCentered() {
		this.centered = true;
		return this;
	}

	@Override
	public void drawLabel(Minecraft mc, int mouseX, int mouseY) {
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		if (this.visible) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			this.drawLabelBackground(mc, mouseX, mouseY);
			int i = this.y + this.height / 2 + this.border / 2;
			int j = i - this.labels.size() * 10 / 2;

			for (int k = 0; k < this.labels.size(); ++k) {
				if (this.centered) {
					this.drawCenteredString(this.fontRenderer, this.labels.get(k), this.x + this.width / 2, j + k * 10, this.textColor);
				} else {
					this.drawString(this.fontRenderer, this.labels.get(k), this.x, j + k * 10, this.textColor);
				}
			}
		}
	}

	@Override
	protected void drawLabelBackground(Minecraft mcIn, int p_146160_2_, int p_146160_3_) {
		if (this.labelBgEnabled) {
			int i = this.width + this.border * 2;
			int j = this.height + this.border * 2;
			int k = this.x - this.border;
			int l = this.y - this.border;
			drawRect(k, l, k + i, l + j, this.backColor);
			this.drawHorizontalLine(k, k + i, l, this.ulColor);
			this.drawHorizontalLine(k, k + i, l + j, this.brColor);
			this.drawVerticalLine(k, l, l + j, this.ulColor);
			this.drawVerticalLine(k + i, l, l + j, this.brColor);
		}
	}

	public boolean isMouseOver() {
		return this.hovered;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public void setLabelBgEnabled(boolean labelBgEnabled) {
		this.labelBgEnabled = labelBgEnabled;
	}

	public void setBackColor(int backColor) {
		this.backColor = backColor;
	}

	public void setUlColor(int ulColor) {
		this.ulColor = ulColor;
	}

	public void setBrColor(int brColor) {
		this.brColor = brColor;
	}

	public void setBorder(int border) {
		this.border = border;
	}

}
