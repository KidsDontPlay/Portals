package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.gui.ContainerPortal;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.inventory.Container;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageButton implements IMessage {

	int id;

	public MessageButton() {
	}

	public MessageButton(int id) {
		super();
		this.id = id;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(id);
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
						Upgrade upgrade = Upgrade.values()[tile.getStacks()[message.id].getItemDamage()];
						System.out.println(upgrade);
					}
				}
			});
			return null;
		}
	}
}
