package mrriegel.portals.items;

import java.util.List;

import mrriegel.limelib.item.CommonItem;
import mrriegel.portals.gui.GuiUpgrade;
import mrriegel.portals.gui.GuiUpgradeColor;
import mrriegel.portals.gui.GuiUpgradeDirection;
import mrriegel.portals.gui.GuiUpgradeParticle;
import mrriegel.portals.tile.TileController;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.text.WordUtils;

public class ItemUpgrade extends CommonItem {

	public enum Upgrade {
		COLOR(true), DIRECTION(true), PARTICLE(true);

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
			}
			return null;
		}
	}

	public ItemUpgrade() {
		super("upgrade");
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		setHasSubtypes(true);
	}

	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (Upgrade u : Upgrade.values())
			subItems.add(new ItemStack(itemIn, 1, u.ordinal()));
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
