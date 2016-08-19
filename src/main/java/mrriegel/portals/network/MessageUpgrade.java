package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.gui.ContainerPortal;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpgrade implements IMessage {
	NBTTagCompound nbt;

	public MessageUpgrade() {
	}

	public MessageUpgrade(NBTTagCompound nbt) {
		super();
		this.nbt = nbt;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		nbt = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, nbt);
	}

	public static class Handler implements IMessageHandler<MessageUpgrade, IMessage> {

		@Override
		public IMessage onMessage(final MessageUpgrade message, final MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					TileController tile = (TileController) ctx.getServerHandler().playerEntity.getServerWorld().getTileEntity(BlockPos.fromLong(message.nbt.getLong("pos")));
					switch (Upgrade.values()[message.nbt.getInteger("id")]) {
					case CAMOUFLAGE:
						break;
					case DIRECTION:
						tile.setLooking(tile.getLooking().rotateAround(Axis.Y));
						break;
					case ENERGY:
						break;
					case FLUID:
						break;
					case ITEM:
						break;
					case PARTICLE:
						break;
					case REDSTONE:
						break;
					}
					tile.sync();
				}
			});
			return null;
		}
	}
}
