package mrriegel.portals.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiUpgrade extends GuiScreen {

	GuiPortal parent;

	public GuiUpgrade(GuiPortal parent) {
		super();
		this.parent = parent;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Minecraft.getMinecraft().currentScreen = parent;
	}

}
