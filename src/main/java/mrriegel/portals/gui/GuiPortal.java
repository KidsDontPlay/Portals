package mrriegel.portals.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import mrriegel.portals.Portals;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.ConfigGuiType;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSelectString;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.IConfigEntry;
import net.minecraftforge.fml.client.config.GuiEditArrayEntries.IArrayEntry;

public class GuiPortal extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Portals.MODID + ":textures/gui/portal.png");

	public GuiPortal(Container inventorySlotsIn) {
		super(inventorySlotsIn);
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
			if (inv.getStackInSlot(k) == null) {
				buttonList.get(k).enabled = false;
				buttonList.get(k).visible = false;
			} else {
				buttonList.get(k).enabled = true;
				buttonList.get(k).visible = true;
				buttonList.get(k).displayString = Upgrade.values()[inv.getStackInSlot(k).getItemDamage()].name();
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addButtons();
	}

	void addButtons() {
		for (int i = 0; i < 8; i++) {
			buttonList.add(new GuiButtonExt(i, 27 + guiLeft, 8 + i * 18 + guiTop, 60, 16, ""));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		Map<Object, String> map = Maps.newHashMap();
		map.put("kak", "kak");
		map.put("simon", "simon");
		Minecraft.getMinecraft().displayGuiScreen(new GuiSelectString(this, new DummyConfigElement("aba", "dam", ConfigGuiType.STRING, "de"), 0, map, "simon", true));
	}

}
