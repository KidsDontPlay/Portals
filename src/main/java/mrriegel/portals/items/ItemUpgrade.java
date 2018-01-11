package mrriegel.portals.items;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.item.CommonSubtypeItem;
import mrriegel.portals.gui.GuiUpgrade;
import mrriegel.portals.gui.GuiUpgradeColor;
import mrriegel.portals.gui.GuiUpgradeDirection;
import mrriegel.portals.gui.GuiUpgradeParticle;
import mrriegel.portals.gui.GuiUpgradeRedstone;
import mrriegel.portals.tile.TileController;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends CommonSubtypeItem {

	public enum Upgrade {
		COLOR(true), DIRECTION(true), PARTICLE(true), REDSTONE(true);

		public boolean hasButton;

		private Upgrade(boolean hasButton) {
			this.hasButton = hasButton;
		}

		public GuiUpgrade getGUI(TileController tile) {
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

	public ItemUpgrade() {
		super("upgrade", Upgrade.values().length);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + Upgrade.values()[stack.getItemDamage()].toString().toLowerCase();
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return WordUtils.capitalize(Upgrade.values()[stack.getItemDamage()].name().toLowerCase()) + " Upgrade";
	}

}
