package mrriegel.portals.gui;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import mrriegel.portals.PortalData;
import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageButton;
import mrriegel.portals.network.MessageName;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

public class GuiPortal extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Portals.MODID + ":textures/gui/portal.png");

	private GuiTextField name;
	private TileController tile;

	private List<String> tiles;
	private List<GuiButtonExt> buttons;

	public GuiPortal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
		tile = ((ContainerPortal) inventorySlotsIn).tile;
		ySize = 238;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
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
		tiles = Lists.newArrayList(PortalData.get(tile.getWorld()).getNames());
		System.out.println("s: " + tiles.size());
		Collections.sort(tiles);
		buttons = Lists.newArrayList();
		for (int i = 0; i < Math.min(4, tiles.size()); i++) {
			buttons.add(new GuiButtonExt(i + 20, 72 + guiLeft, 38 + i * 18 + guiTop, 60, 16, "ne") {
				@Override
				public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
					return this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
				}
			});
		}
		buttonList.addAll(buttons);

	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id < 8)
			PacketHandler.INSTANCE.sendToServer(new MessageButton(button.id));
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

}
