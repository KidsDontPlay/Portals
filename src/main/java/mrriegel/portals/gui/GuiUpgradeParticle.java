package mrriegel.portals.gui;

import java.io.IOException;

import org.apache.commons.lang3.mutable.MutableInt;

import mrriegel.limelib.gui.GuiDrawer;
import mrriegel.limelib.gui.button.CommonGuiButton;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

public class GuiUpgradeParticle extends GuiUpgrade {

	GuiButton color;
	MutableInt i;

	public GuiUpgradeParticle(TileController tile, Upgrade upgrade) {
		super(tile, upgrade);
		i = new MutableInt(tile.getColorParticle());
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(color = new CommonGuiButton(0, guiLeft + 10, guiTop + 30, 80, 20, "Color"));
	}

	@Override
	protected void onClosed() {
		super.onClosed();
		NBTTagCompound nbt = getTag();
		nbt.setInteger("color", i.intValue());
		NBTHelper.set(nbt, "kind", TileController.UPGRADE);
		tile.sendMessage(nbt);
		tile.handleMessage(mc.player, nbt);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == color)
			GuiDrawer.openGui(new GuiColorSlider(this, i));
	}

}
