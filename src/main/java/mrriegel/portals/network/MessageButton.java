package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.gui.ContainerPortal;
import mrriegel.portals.gui.GuiLabelExt;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageButton implements IMessage {

	int id, dim;
	BlockPos pos;

	public MessageButton() {
	}

	public MessageButton(int id, int dim, BlockPos pos) {
		super();
		this.id = id;
		this.dim = dim;
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
		dim = buf.readInt();
		pos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
		buf.writeInt(dim);
		buf.writeLong(pos.toLong());
	}

	public static class Handler implements IMessageHandler<MessageButton, IMessage> {

		@Override
		public IMessage onMessage(final MessageButton message, final MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Container con = ctx.getServerHandler().playerEntity.openContainer;
					if (con instanceof ContainerPortal) {
						TileController tile = ((ContainerPortal) con).tile;
						if (message.id == 1000) {
							tile.setTarget(new GlobalBlockPos(message.pos, message.dim));
						} else {
							if(tile.getTarget()!=null)
								tile.teleport(ctx.getServerHandler().playerEntity);
							Upgrade upgrade = Upgrade.values()[tile.getStacks()[message.id].getItemDamage()];
							System.out.println(upgrade);
						}
					}
				}
			});
			return null;
		}
	}
}
