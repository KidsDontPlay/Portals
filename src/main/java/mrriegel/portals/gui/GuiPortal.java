package mrriegel.portals.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mrriegel.limelib.gui.CommonGuiContainer;
import mrriegel.limelib.gui.GuiDrawer.Direction;
import mrriegel.limelib.gui.element.GuiButtonArrow;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.PortalData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GuiPortal extends CommonGuiContainer {

	private GuiTextField name;
	private TileController tile;
	private int visibleButtons, currentPos, maxPos;
	private List<GuiButtonExt> targetButtons;
	private List<String> targets;
	private Map<String, String> targetMap;
	private String currentTarget;
	private GuiButton up, down;

	public GuiPortal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		tile = ((ContainerPortal) inventorySlotsIn).tile;
		ySize = 238;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawer.drawBackgroundTexture();
		drawer.drawSlots(7, 7, 1, 8);
		drawer.drawPlayerSlots(7, 155);
		drawer.drawRectangle(88, 137, 81, 14);
		drawer.drawTextfield(name);
		name.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRendererObj.drawString("Name:", 90, 7, 0);
		fontRendererObj.drawString("Target: " + (currentTarget != null ? currentTarget : ""), 90, 140, currentTarget != null && !currentTarget.isEmpty() ? Color.DARK_GRAY.darker().getRGB() : Color.RED.getRGB());
		IInventory inv = ((ContainerPortal) inventorySlots).tmp;
		for (int k = 0; k < inv.getSizeInventory(); k++) {
			if (inv.getStackInSlot(k) != null && Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].hasButton && buttonList.get(k).isMouseOver()) {
				drawHoveringText(Lists.newArrayList(I18n.format("tooltip.portals." + Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name().toLowerCase())), mouseX - guiLeft, mouseY - guiTop);
			}
		}
		for (GuiButton b : targetButtons) {
			if (b.isMouseOver())
				drawHoveringText(Lists.newArrayList(targetMap.get(b.displayString)), mouseX - guiLeft, mouseY - guiTop);
		}
	}
	
	@Override
	protected void onUpdate() {
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
		for (int i = 0; i < targetButtons.size(); i++) {
			GuiButtonExt b = targetButtons.get(i);
			b.displayString = targets.get(i + currentPos);
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
		name.setMaxStringLength(15);
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
		up = new GuiButtonArrow(2000, 153 + guiLeft, 38 + guiTop, Direction.UP);
		up.enabled = false;
		buttonList.add(up);
		down = new GuiButtonArrow(2001, 153 + guiLeft, 114 + guiTop, Direction.DOWN);
		if (maxPos == 0)
			down.enabled = false;
		buttonList.add(down);
		currentTarget = tile.getTarget() != null && ((TileController) tile.getTarget().getTile(tile.getWorld())) != null ? ((TileController) tile.getTarget().getTile(tile.getWorld())).getName() : "";
		targetMap = Maps.newHashMap();
		for (String s : targets) {
			TileController t = PortalData.get(tile.getWorld()).getTile(s);
			if (t != null)
				targetMap.put(s, WordUtils.capitalizeFully(t.getWorld().provider.getDimensionType().toString()) + ", x:" + t.getPos().getX() + " y:" + t.getPos().getY() + " z:" + t.getPos().getZ());
		}

	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		boolean isOverTextField = mouseX >= name.xPosition && mouseY >= name.yPosition && mouseX < name.xPosition + this.width && mouseY < name.yPosition + name.height;
		if (state == 1 && isOverTextField)
			name.setText("");
		name.setFocused(isOverTextField);
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
		} else if (button.id == 2001) {
			if (currentPos < maxPos)
				currentPos++;
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTHelper.setInt(nbt, "kind", tile.BUTTON);
			NBTHelper.setString(nbt, "target", button.displayString);
			tile.sendMessage(nbt);
			TileController target = PortalData.get(tile.getWorld()).getTile(button.displayString);
			if (target != null) {
				tile.setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
				currentTarget = ((TileController) tile.getTarget().getTile(tile.getWorld())).getName();
			}
		}
		up.enabled = currentPos != 0;
		down.enabled = currentPos != maxPos;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		tile.setName(name.getText());
		NBTTagCompound nbt = new NBTTagCompound();
		NBTHelper.setInt(nbt, "kind", tile.NAME);
		NBTHelper.setString(nbt, "name", name.getText());
		tile.sendMessage(nbt);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.checkHotbarKeys(keyCode)) {
			if (this.name.textboxKeyTyped(typedChar, keyCode)) {
				tile.setName(name.getText());
				NBTTagCompound nbt = new NBTTagCompound();
				NBTHelper.setInt(nbt, "kind", tile.NAME);
				NBTHelper.setString(nbt, "name", name.getText());
				tile.sendMessage(nbt);
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int i = Mouse.getX() * this.width / this.mc.displayWidth;
		int j = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
		if (!targetButtons.isEmpty() && i > targetButtons.get(0).xPosition && i < down.xPosition + down.width && j > targetButtons.get(0).yPosition && j < down.yPosition + down.height) {
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
