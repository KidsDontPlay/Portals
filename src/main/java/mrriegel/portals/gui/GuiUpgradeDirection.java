package mrriegel.portals.gui;

import java.io.IOException;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.network.MessageUpgrade;
import mrriegel.portals.network.PacketHandler;
import mrriegel.portals.tile.TileController;

public class GuiUpgradeDirection extends GuiUpgrade {

	public GuiUpgradeDirection(GuiPortal parent, TileController tile, Upgrade upgrade) {
		super(parent, tile, upgrade);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(new GuiButton(0, guiLeft + 20, guiTop + 50, 60, 20, WordUtils.capitalize(tile.getLooking().getName2().toLowerCase())));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		tile.setLooking(tile.getLooking().rotateAround(Axis.Y));
		button.displayString=WordUtils.capitalize(tile.getLooking().getName2().toLowerCase());
		PacketHandler.INSTANCE.sendToServer(new MessageUpgrade(getTag()));
	}

}
