package mrriegel.classicportals.items;

import org.apache.commons.lang3.text.WordUtils;

import mrriegel.limelib.item.CommonSubtypeItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemUpgrade extends CommonSubtypeItem {

	public ItemUpgrade() {
		super("upgrade", Upgrade.values().length);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + Upgrade.values()[stack.getItemDamage()].toString().toLowerCase();
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return WordUtils.capitalize(Upgrade.values()[stack.getItemDamage()].name().toLowerCase()) + " Chip";
	}

}
