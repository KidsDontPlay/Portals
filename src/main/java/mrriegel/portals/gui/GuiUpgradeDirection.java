package mrriegel.portals.gui;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;

public class GuiUpgradeDirection extends GuiUpgrade {

	public GuiUpgradeDirection(TileController tile, Upgrade upgrade) {
		super(tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, guiLeft + 20, guiTop + 45, 60, 20, WordUtils.capitalize(tile.getLooking().getName2().toLowerCase())));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		tile.setLooking(tile.getLooking().rotateAround(Axis.Y));
		button.displayString = WordUtils.capitalize(tile.getLooking().getName2().toLowerCase());
		NBTTagCompound nbt = getTag();
		NBTHelper.set(nbt, "kind", TileController.UPGRADE);
		tile.sendMessage(nbt);
	}

}
