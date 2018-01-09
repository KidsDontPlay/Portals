package mrriegel.portals.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import mrriegel.limelib.gui.CommonGuiContainer;
import mrriegel.limelib.gui.GuiDrawer;
import mrriegel.limelib.gui.GuiDrawer.Direction;
import mrriegel.limelib.gui.button.CommonGuiButton;
import mrriegel.limelib.gui.button.CommonGuiButton.Design;
import mrriegel.limelib.gui.button.GuiButtonArrow;
import mrriegel.limelib.helper.ColorHelper;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

public class GuiPortal extends CommonGuiContainer {

	private GuiTextField name;
	private TileController tile;
	private int visibleButtons, currentPos, maxPos;
	private List<GuiButton> targetButtons;
	private List<String> targets;
	//	private Map<String, String> targetMap;
	private String currentTarget;
	private GuiButton up, down;

	public GuiPortal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		tile = ((ContainerPortal) inventorySlotsIn).getTile();
		ySize = 166;
		xSize = 220;

	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		for (int i = 0; i < 4; i++) {
			GuiButton b = new CommonGuiButton(i, 196 + guiLeft, 12 + 18 * 4 + i * 18 + guiTop, 16, 16, "...");
			b.visible = false;
			buttonList.add(b);
		}
		name = new GuiTextField(0, fontRenderer, 36 + guiLeft, 7 + guiTop, 90, fontRenderer.FONT_HEIGHT);
		name.setMaxStringLength(15);
		name.setTextColor(16777215);
		name.setText(((ContainerPortal) inventorySlots).getTile().getName());
		name.setFocused(true);
		targets = new ArrayList<>(PortalWorldData.getData(mc.world).getNames());
		targets.remove(tile.getName());
		targets.sort((s1, s2) -> s1.toLowerCase().compareTo(s2.toLowerCase()));
		visibleButtons = Math.min(5, targets.size());
		targetButtons = Lists.newArrayList();
		for (int i = 0; i < visibleButtons; i++) {
			targetButtons.add(new CommonGuiButton(i + 1000, 10 + guiLeft, 21 + i * (fontRenderer.FONT_HEIGHT + 2) + guiTop, 95, fontRenderer.FONT_HEIGHT + 2, "") {
				@Override
				public void drawButton(Minecraft mc, int mouseX, int mouseY, float partial) {
					if (isMouseOver())
						drawer.drawColoredRectangle(x - 1, y, width, height, ColorHelper.getRGB(0x040404, 100));
					super.drawButton(mc, mouseX, mouseY, partial);
				}
			}.setDesign(Design.NONE));
		}
		buttonList.addAll(targetButtons);
		maxPos = targets.size() - visibleButtons;
		up = new GuiButtonArrow(2000, 108 + guiLeft, 20 + guiTop, Direction.UP);
		up.enabled = false;
		buttonList.add(up);
		down = new GuiButtonArrow(2001, 108 + guiLeft, 68 + guiTop, Direction.DOWN);
		if (maxPos == 0)
			down.enabled = false;
		buttonList.add(down);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		drawer.drawBackgroundTexture();
		drawer.drawFramedRectangle(7, 19, 100, 60);
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawer.drawSlots(175, 11 + 18 * 4, 1, 4);
		drawer.drawPlayerSlots(7, 11 + 18 * 4);
		double percent = maxPos == 0. ? 0. : (currentPos + 0.) / (maxPos + 0.);
		drawer.drawColoredRectangle(110, 30 + (int) (percent * 36), 14, 2, 0xFF050505);
		name.drawTextBox();
		fontRenderer.drawString("Name:", 7 + guiLeft, 7 + guiTop, 0x040404, !true);
		fontRenderer.drawString("Target: " + (currentTarget != null ? currentTarget : ""), 100 + guiLeft, 50 + guiTop, currentTarget != null && !currentTarget.isEmpty() ? Color.DARK_GRAY.darker().getRGB() : Color.RED.getRGB());
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		IInventory inv = ((ContainerPortal) inventorySlots).tmp;
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			if (!inv.getStackInSlot(k).isEmpty() && Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton && buttonList.get(k).isMouseOver()) {
				drawHoveringText(Lists.newArrayList(I18n.format("tooltip.portals." + Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name().toLowerCase())), mouseX - guiLeft, mouseY - guiTop);
			}
		}
		PortalWorldData pwd = PortalWorldData.getData(mc.world);
		for (GuiButton b : targetButtons) {
			if (b.isMouseOver()) {
				GlobalBlockPos gbp = pwd.posMap.get(b.displayString);
				if (gbp != null)
					drawHoveringText(Lists.newArrayList(WordUtils.capitalizeFully("Dim:" + gbp.getDimension() + ", x:" + gbp.getPos().getX() + " y:" + gbp.getPos().getY() + " z:" + gbp.getPos().getZ())), mouseX - guiLeft, mouseY - guiTop);
			}
		}
	}

	@Override
	public void updateScreen() {
		IInventory inv = ((ContainerPortal) inventorySlots).tmp;
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			if (inv.getStackInSlot(k).isEmpty() || !Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton) {
				buttonList.get(k).visible = false;
			} else if (Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton) {
				buttonList.get(k).visible = true;
				((CommonGuiButton) buttonList.get(k)).setTooltip(WordUtils.capitalize(Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name().toLowerCase()));
			}
		}
		for (int i = 0; i < targetButtons.size(); i++) {
			GuiButton b = targetButtons.get(i);
			b.displayString = targets.get(i + currentPos);
		}
		currentTarget = tile.getTargetName();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		name.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 1)
			name.setText("");
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 4) {
			GuiUpgrade gui = Upgrade.values()[((ContainerPortal) inventorySlots).tmp.getStackInSlot(button.id).getItemDamage()].getGUI(tile);
			if (gui != null)
				GuiDrawer.openGui(gui);
		} else if (button.id == 2000) {
			if (currentPos > 0)
				currentPos--;
		} else if (button.id == 2001) {
			if (currentPos < maxPos)
				currentPos++;
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTHelper.set(nbt, "kind", TileController.BUTTON);
			NBTHelper.set(nbt, "target", button.displayString);
			tile.sendMessage(nbt);
			tile.handleMessage(mc.player, nbt);
		}
		up.enabled = currentPos != 0;
		down.enabled = currentPos != maxPos;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		NBTTagCompound nbt = new NBTTagCompound();
		NBTHelper.set(nbt, "kind", TileController.NAME);
		NBTHelper.set(nbt, "name", name.getText());
		tile.sendMessage(nbt);
		tile.handleMessage(mc.player, nbt);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.checkHotbarKeys(keyCode)) {
			if (this.name.textboxKeyTyped(typedChar, keyCode)) {
				NBTTagCompound nbt = new NBTTagCompound();
				NBTHelper.set(nbt, "kind", TileController.NAME);
				NBTHelper.set(nbt, "name", name.getText());
				tile.sendMessage(nbt);
				tile.handleMessage(mc.player, nbt);
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int i = GuiDrawer.getMouseX();
		int j = GuiDrawer.getMouseY();
		if (!targetButtons.isEmpty() && i > targetButtons.get(0).x && i < down.x + down.width && j > targetButtons.get(0).y && j < down.y + down.height) {
			int mouse = Mouse.getEventDWheel();
			if (mouse == 0)
				return;
			if (mouse > 0 && currentPos > 0)
				currentPos--;
			if (mouse < 0 && currentPos < maxPos)
				currentPos++;
			up.enabled = currentPos != 0;
			down.enabled = currentPos != maxPos;
		}
	}

}
