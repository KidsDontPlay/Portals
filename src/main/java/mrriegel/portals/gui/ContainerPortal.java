package mrriegel.portals.gui;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import mrriegel.limelib.gui.CommonContainerTile;
import mrriegel.portals.init.ModItems;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class ContainerPortal extends CommonContainerTile<TileController> {

	IInventory tmp;

	public ContainerPortal(final TileController tile, InventoryPlayer invPlayer) {
		super(invPlayer, tile, Pair.of("tile", new InventoryBasic("null", false, tile.getStacks().size()) {
			@Override
			public boolean isItemValidForSlot(int index, ItemStack stack) {
				return stack.getItem() == ModItems.upgrade;
			}
		}));
		this.save = tile;
		tmp = (IInventory) invs.get("tile");
		for (int i = 0; i < tile.getStacks().size(); i++) {
			ItemStack k = tile.getStacks().get(i);
			tmp.setInventorySlotContents(i, k);
		}
		this.save.sync();
		if (!tile.getWorld().isRemote) {
			if (tile.isActive() && !TileController.isPortalActive(tile.getTarget()))
				tile.deactivate();
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		for (int i = 0; i < save.getStacks().size(); i++) {
			save.getStacks().set(i, tmp.getStackInSlot(i));
		}
		save.sync();
	}

	@Override
	protected void initSlots() {
		initPlayerSlots(8, 12 + 18 * 4);
		initSlots(invs.get("tile"), 176, 12 + 18 * 4, 1, 4);
	}

	@Override
	protected List<Area> allowedSlots(ItemStack stack, IInventory inv, int index) {
		List<Area> lis = Lists.newArrayList();
		lis.add(inv == invPlayer ? stack.getItem() == ModItems.upgrade ? getAreaForEntireInv(tmp) : null : getAreaForEntireInv(invPlayer));
		return lis;
	}

}
