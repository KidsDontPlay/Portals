package mrriegel.portals.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageButton;
import mrriegel.portals.network.MessageName;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.GlobalBlockPos;
import mrriegel.portals.util.PortalData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

public class GuiPortal extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Portals.MODID + ":textures/gui/portal.png");

	private GuiTextField name;
	private TileController tile;
	private int visibleButtons, currentPos, maxPos;
	private List<GuiButtonExt> targetButtons;
	private List<String> targets;
	private String currentTarget;

	public GuiPortal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		tile = ((ContainerPortal) inventorySlotsIn).tile;
		ySize = 238;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
		IInventory inv = ((ContainerPortal) inventorySlots).tmp;
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			if (inv.getStackInSlot(k) == null || !Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton) {
				buttonList.get(k).enabled = false;
				buttonList.get(k).visible = false;
			} else if (Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton) {
				buttonList.get(k).enabled = true;
				buttonList.get(k).visible = true;
				buttonList.get(k).displayString = WordUtils.capitalize(Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name().toLowerCase());
			}
		}
		name.drawTextBox();
		for (GuiLabel label : labelList) {
			if (((GuiLabelExt) label).isMouseOver()) {
				((GuiLabelExt) label).setTextColor(0xfff0);
				((GuiLabelExt) label).setLabelBgEnabled(true);
			} else {
				((GuiLabelExt) label).setTextColor(0xffffff);
				((GuiLabelExt) label).setLabelBgEnabled(true);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRendererObj.drawString("Name:", 90, 7, 0);
		fontRendererObj.drawString("Target: " + (currentTarget != null ? currentTarget : ""), 90, 140, currentTarget != null && !currentTarget.isEmpty() ? Color.DARK_GRAY.getRGB() : Color.RED.getRGB());
		for (int i = 0; i < targetButtons.size(); i++) {
			GuiButtonExt b = targetButtons.get(i);
			b.displayString = targets.get(i + currentPos);
		}
		IInventory inv = ((ContainerPortal) inventorySlots).tmp;
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			if (inv.getStackInSlot(k) != null&&Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton && buttonList.get(k).isMouseOver()) {
				drawHoveringText(Lists.newArrayList(I18n.format("tooltip.portals." + Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name().toLowerCase())), mouseX-guiLeft, mouseY-guiTop);
			}
		}

	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		for (int i = 0; i < 8; i++) {
			buttonList.add(new GuiButtonExt(i, 27 + guiLeft, 8 + i * 18 + guiTop, 60, 16, ""));
		}
		name = new GuiTextField(0, fontRendererObj, 92 + guiLeft, 22 + guiTop, 70, fontRendererObj.FONT_HEIGHT);
		name.setMaxStringLength(25);
		name.setEnableBackgroundDrawing(false);
		name.setVisible(true);
		name.setTextColor(16777215);
		name.setText(((ContainerPortal) inventorySlots).tile.getName());
		name.setFocused(true);
		targets = Lists.newArrayList(PortalData.get(tile.getWorld()).getNames());
		targets.remove(tile.getName());
		Collections.sort(targets);
		visibleButtons = Math.min(5, targets.size());
		targetButtons = Lists.newArrayList();
		for (int i = 0; i < visibleButtons; i++) {
			targetButtons.add(new GuiButtonExt(i + 1000, 92 + guiLeft, 38 + i * 18 + guiTop, 60, 16, ""));
		}
		buttonList.addAll(targetButtons);
		maxPos = targets.size() - visibleButtons;
		GuiButtonExt b1 = new GuiButtonExt(2000, 153 + guiLeft, 38 + guiTop, 18, 12, "^");
		b1.enabled = false;
		buttonList.add(b1);
		GuiButtonExt b2 = new GuiButtonExt(2001, 153 + guiLeft, 114 + guiTop, 18, 12, "v");
		if (maxPos == 0)
			b2.enabled = false;
		buttonList.add(b2);
		currentTarget = tile.getTarget() != null && ((TileController) tile.getTarget().getTile(tile.getWorld())) != null ? ((TileController) tile.getTarget().getTile(tile.getWorld())).getName() : "";

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 8) {
			GuiUpgrade gui = Upgrade.values()[((ContainerPortal) inventorySlots).tmp.getStackInSlot(button.id).getItemDamage()].getGUI(this, tile);
			if (gui != null)
				mc.displayGuiScreen(gui);
		} else if (button.id == 2000) {
			if (currentPos > 0)
				currentPos--;
			button.enabled = currentPos != 0;
		} else if (button.id == 2001) {
			if (currentPos < maxPos)
				currentPos++;
			button.enabled = currentPos != maxPos;
		} else {

			PacketHandler.INSTANCE.sendToServer(new MessageButton(button.displayString));
			TileController target = PortalData.get(tile.getWorld()).getTile(button.displayString);
			if (target != null) {
				tile.setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
				currentTarget = ((TileController) tile.getTarget().getTile(tile.getWorld())).getName();
			}
		}
		for (GuiButton b : buttonList) {
			if (b.id == 2000) {
				b.enabled = currentPos != 0;
			} else if (b.id == 2001) {
				b.enabled = currentPos != maxPos;
			}
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		tile.setName(name.getText());
		PacketHandler.INSTANCE.sendToServer(new MessageName(name.getText(), tile.getPos()));
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.checkHotbarKeys(keyCode)) {
			if (this.name.textboxKeyTyped(typedChar, keyCode)) {
				tile.setName(name.getText());
				PacketHandler.INSTANCE.sendToServer(new MessageName(name.getText(), tile.getPos()));
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}

	// @Override
	// protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	// throws IOException {
	// super.mouseClicked(mouseX, mouseY, mouseButton);
	// for (GuiLabel label : labelList) {
	// if (((GuiLabelExt) label).isMouseOver()) {
	// TileController target =
	// PortalData.get(tile.getWorld()).getTile(((GuiLabelExt)
	// label).labels.get(0));
	// tile.setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
	// PacketHandler.INSTANCE.sendToServer(new MessageButton(1000,
	// target.getWorld().provider.getDimension(), target.getPos()));
	// }
	// }
	// }
}
