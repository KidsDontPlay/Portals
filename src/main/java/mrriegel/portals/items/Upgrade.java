package mrriegel.portals.items;

import mrriegel.portals.gui.GuiUpgradeColor;
import mrriegel.portals.gui.GuiUpgradeDirection;
import mrriegel.portals.gui.GuiUpgradeParticle;
import mrriegel.portals.gui.GuiUpgradeRedstone;
import mrriegel.portals.tile.TileController;

public enum Upgrade {
	COLOR(true), DIRECTION(true), PARTICLE(true), REDSTONE(true);

	public boolean hasButton;

	private Upgrade(boolean hasButton) {
		this.hasButton = hasButton;
	}

	public Object getGUI(TileController tile) {
		switch (this) {
		case COLOR:
			return new GuiUpgradeColor(tile, this);
		case DIRECTION:
			return new GuiUpgradeDirection(tile, this);
		case PARTICLE:
			return new GuiUpgradeParticle(tile, this);
		case REDSTONE:
			return new GuiUpgradeRedstone(tile, this);
		}
		return null;
	}
}
