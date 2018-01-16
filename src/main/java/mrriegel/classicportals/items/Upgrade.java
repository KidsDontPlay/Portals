package mrriegel.classicportals.items;

import mrriegel.classicportals.gui.GuiUpgradeColor;
import mrriegel.classicportals.gui.GuiUpgradeDirection;
import mrriegel.classicportals.gui.GuiUpgradeParticle;
import mrriegel.classicportals.gui.GuiUpgradeRedstone;
import mrriegel.classicportals.tile.TileController;

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
