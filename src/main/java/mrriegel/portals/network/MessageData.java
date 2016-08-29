package mrriegel.portals.network;

import io.netty.buffer.ByteBuf;

import java.util.Set;

import mrriegel.portals.util.GlobalBlockPos;
import mrriegel.portals.util.PortalData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class MessageData implements IMessage {
	Set<GlobalBlockPos> data;

	public MessageData() {
	}

	public MessageData(Set<GlobalBlockPos> data) {
		super();
		this.data = data;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		data = new Gson().fromJson(ByteBufUtils.readUTF8String(buf), new TypeToken<Set<GlobalBlockPos>>() {
		}.getType());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, new Gson().toJson(data));
	}

	public static class Handler implements IMessageHandler<MessageData, IMessage> {

		@Override
		public IMessage onMessage(final MessageData message, final MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(new Runnable() {
				@Override
				public void run() {
					PortalData.get(Minecraft.getMinecraft().theWorld).valids = message.data;
				}
			});
			return null;
		}
	}
}
