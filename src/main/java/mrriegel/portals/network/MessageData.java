package mrriegel.portals.network;

import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.gui.ContainerPortal;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.tile.TileController;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
		System.out.println("read:");
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, new Gson().toJson(data));
		System.out.println("write:");
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
