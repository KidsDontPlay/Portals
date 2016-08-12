package mrriegel.portals.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageButton;
import mrriegel.portals.network.MessageName;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiButtonExt;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

public class GuiPortal extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Portals.MODID + ":textures/gui/portal.png");

	private GuiTextField name;
	private TileController tile;

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
				buttonList.get(k).displayString = Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name();
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
		List<String> tiles = Lists.newArrayList(PortalData.get(tile.getWorld()).getNames());
		tiles.remove(tile.getName());
		Collections.sort(tiles);
		for (int i = 0; i < Math.min(4, tiles.size()); i++) {
			GuiLabelExt l = new GuiLabelExt(fontRendererObj, i, 82 + guiLeft, 38 + i * 18 + guiTop, 60, 16, 0);
			l.addLine(tiles.get(i));
			l.setBorder(2);
//			l.setBackColor(0xff00cc);
//			l.setBrColor(0x00ffcc);
//			l.setUlColor(0xccff00);
			labelList.add(l);
		}

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		PacketHandler.INSTANCE.sendToServer(new MessageButton(button.id, 0, BlockPos.ORIGIN));
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

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (GuiLabel label : labelList) {
			if (((GuiLabelExt) label).isMouseOver()) {
				TileController target = PortalData.get(tile.getWorld()).getTile(((GuiLabelExt) label).labels.get(0));
				tile.setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
				PacketHandler.INSTANCE.sendToServer(new MessageButton(1000, target.getWorld().provider.getDimension(), target.getPos()));
			}
		}
	}

	void displayUpgrade() {
		mc.displayGuiScreen(new GuiUpgrade(this));
	}
}
