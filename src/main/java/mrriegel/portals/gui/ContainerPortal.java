package mrriegel.portals.gui;

import mrriegel.portals.init.ModItems;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPortal extends Container {

	public TileController tile;
	InventoryPlayer invPlayer;
	InventoryBasic tmp;

	public ContainerPortal(final TileController tile, InventoryPlayer invPlayer) {
		this.tile = tile;
		this.invPlayer = invPlayer;
		tmp = new InventoryBasic("null", false, tile.getStacks().length);
		for (int i = 0; i < tile.getStacks().length; i++) {
			ItemStack k = tile.getStacks()[i];
			tmp.setInventorySlotContents(i, k);
		}
		for (int i = 0; i < 8; ++i) {
			this.addSlotToContainer(new Slot(tmp, i, 8, 8 + i * 18) {
				@Override
				public boolean isItemValid(ItemStack stack) {
					if (stack == null)
						return false;
					boolean in = false;
					for (ItemStack k : tile.getStacks()) {
						if (k != null && k.getItemDamage() == stack.getItemDamage()) {
							in = true;
							break;
						}
					}
					return !in && stack.getItem() == ModItems.upgrade;
				}
			});
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlotToContainer(new Slot(invPlayer, i1 + k * 9 + 9, 8 + i1 * 18, 156 + k * 18));
			}
		}
		for (int l = 0; l < 9; ++l) {
			this.addSlotToContainer(new Slot(invPlayer, l, 8 + l * 18, 214));
		}

		// System.out.println(tile.getTarget());
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		refresh();
		tile.sync();
	}

	private void refresh() {
		for (int i = 0; i < tile.getStacks().length; i++) {
			tile.getStacks()[i] = tmp.getStackInSlot(i);
		}
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
		refresh();
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		return null;
	}

}
