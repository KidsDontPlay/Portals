package mrriegel.portals.items;

import java.util.List;

import mrriegel.portals.gui.GuiPortal;
import mrriegel.portals.gui.GuiUpgrade;
import mrriegel.portals.gui.GuiUpgradeColor;
import mrriegel.portals.gui.GuiUpgradeDirection;
import mrriegel.portals.gui.GuiUpgradeParticle;
import mrriegel.portals.tile.TileController;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends Item {

	public enum Upgrade {
		COLOR(true), DIRECTION(true), PARTICLE(true);

		public boolean hasButton;

		private Upgrade(boolean hasButton) {
			this.hasButton = hasButton;
		}

		public GuiUpgrade getGUI(GuiPortal parent, TileController tile) {
			switch (this) {
			case COLOR:
				return new GuiUpgradeColor(parent, tile, this);
			case DIRECTION:
				return new GuiUpgradeDirection(parent, tile, this);
			case PARTICLE:
				return new GuiUpgradeParticle(parent, tile, this);
			}
			return null;
		}
	}

	public ItemUpgrade() {
		setRegistryName("upgrade");
		setUnlocalizedName(getRegistryName().toString());
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

}
