package mrriegel.portals.items;

import java.util.List;

import mrriegel.portals.gui.GuiPortal;
import mrriegel.portals.gui.GuiUpgrade;
import mrriegel.portals.gui.GuiUpgradeDirection;
import mrriegel.portals.tile.TileController;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends Item {

	public enum Upgrade {
		CAMOUFLAGE(true), DIRECTION(true), MOTION(false), PARTICLE(true), REDSTONE(true), ITEM(true), FLUID(true), ENERGY(true);
		
		public boolean hasButton;

		private Upgrade(boolean hasButton) {
			this.hasButton = hasButton;
		}
		
		public GuiUpgrade getGUI(GuiPortal parent, TileController tile){
			switch (this) {
			case CAMOUFLAGE:
				break;
			case DIRECTION:
				return new GuiUpgradeDirection(parent, tile, this);
			case ENERGY:
				break;
			case FLUID:
				break;
			case ITEM:
				break;
			case MOTION:
				break;
			case PARTICLE:
				break;
			case REDSTONE:
				break;
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
