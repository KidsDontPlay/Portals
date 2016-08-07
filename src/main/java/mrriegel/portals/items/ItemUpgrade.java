package mrriegel.portals.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends Item {

	public enum Upgrade {
		CAMOUFLAGE, DIRECTION, PARTICLE, REDSTONE, ITEM, FLUID, ENERGY;
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
