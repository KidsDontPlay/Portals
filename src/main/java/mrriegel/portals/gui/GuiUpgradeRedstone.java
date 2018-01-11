package mrriegel.portals.gui;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.gui.button.CommonGuiButton;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.portals.items.Upgrade;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.tile.TileController.RedstoneMode;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

public class GuiUpgradeRedstone extends GuiUpgrade {

	public GuiUpgradeRedstone(TileController tile, Upgrade upgrade) {
		super(tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new CommonGuiButton(0, guiLeft + 20, guiTop + 45, 60, 20, WordUtils.capitalize(tile.getRed().name().toLowerCase())));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		tile.setRed(RedstoneMode.values()[(tile.getRed().ordinal() + 1) % RedstoneMode.values().length]);
		button.displayString = WordUtils.capitalize(tile.getRed().name().toLowerCase());
		NBTTagCompound nbt = getTag();
		NBTHelper.set(nbt, "kind", TileController.UPGRADE);
		tile.sendMessage(nbt);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		String k = "";
		switch (tile.getRed()) {
		case OFF:
			k = "Portal is on when not powered.";
			break;
		case ON:
			k = "Portal is on when powered.";
			break;
		case TOGGLE:
			k = "Portal toggles when pulse is received.";
			break;
		default:
			break;
		}
		((CommonGuiButton) buttonList.get(0)).setTooltip(k);
	}

}
