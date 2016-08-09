package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.tile.TileController;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import org.apache.commons.lang3.RandomStringUtils;

public class MessageName implements IMessage {

	String name;
	BlockPos pos;

	public MessageName() {
	}

	public MessageName(String name, BlockPos pos) {
		this.name = name;
		this.pos = pos;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		name = ByteBufUtils.readUTF8String(buf);
		pos = BlockPos.fromLong(buf.readLong());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, name);
		buf.writeLong(pos.toLong());
	}

	public static class Handler implements IMessageHandler<MessageName, IMessage> {
		@Override
		public IMessage onMessage(final MessageName message, final MessageContext ctx) {
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					World world = ctx.getServerHandler().playerEntity.worldObj;
					PortalData data = PortalData.get(world);
					String neu = message.name;
					if (neu.isEmpty())
						neu = RandomStringUtils.random(10, true, true);
					int i = 1;
					while (data.nameOccupied(neu, new GlobalBlockPos(message.pos, world))) {
						neu = "Occupied" + i;
						i++;
					}
					((TileController) world.getTileEntity(message.pos)).setName(neu);
					((TileController) world.getTileEntity(message.pos)).sync();
				}
			});
			return null;
		}
	}
}
