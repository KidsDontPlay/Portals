package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.gui.ContainerPortal;
import mrriegel.portals.tile.TileController;
import mrriegel.portals.util.GlobalBlockPos;
import mrriegel.portals.util.PortalData;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageButton implements IMessage {

	String name;

	public MessageButton() {
	}

	public MessageButton(String name) {
		super();
		this.name = name;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
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
						TileController target = PortalData.get(ctx.getServerHandler().playerEntity.getServerWorld()).getTile(message.name);
						if (target != null && tile != null)
							tile.setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
					}
				}
			});
			return null;
		}
	}
}
