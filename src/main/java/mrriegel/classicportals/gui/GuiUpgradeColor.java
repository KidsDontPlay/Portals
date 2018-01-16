package mrriegel.classicportals.gui;

import java.io.IOException;

import org.apache.commons.lang3.mutable.MutableInt;

import mrriegel.classicportals.items.Upgrade;
import mrriegel.classicportals.tile.TileController;
import mrriegel.limelib.gui.GuiDrawer;
import mrriegel.limelib.gui.button.CommonGuiButton;
import mrriegel.limelib.helper.NBTHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

public class GuiUpgradeColor extends GuiUpgrade {

	GuiButton portal, frame;
	MutableInt portalI, frameI;

	public GuiUpgradeColor(TileController tile, Upgrade upgrade) {
		super(tile, upgrade);
		portalI = new MutableInt(tile.getColorPortal());
		frameI = new MutableInt(tile.getColorFrame());
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(portal = new CommonGuiButton(0, guiLeft + 10, guiTop + 30, 80, 20, "Portal"));
		buttonList.add(frame = new CommonGuiButton(1, guiLeft + 10, guiTop + 64, 80, 20, "Frame"));
	}

	@Override
	protected void onClosed() {
		super.onClosed();
		NBTTagCompound nbt = getTag();
		NBTHelper.set(nbt, "colorP", portalI.intValue());
		NBTHelper.set(nbt, "colorF", frameI.intValue());
		NBTHelper.set(nbt, "kind", TileController.UPGRADE);
		tile.handleMessage(mc.player, nbt);
		tile.sendMessage(nbt);
		tile.repaintPortal();
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button == portal)
			GuiDrawer.openGui(new GuiColorSlider(this, portalI));
		else if (button == frame)
			GuiDrawer.openGui(new GuiColorSlider(this, frameI));
	}

}
