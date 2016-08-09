package mrriegel.portals.network;

import java.io.IOException;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketPortalData implements Packet<INetHandlerPlayClient> {
	Set<GlobalBlockPos> data;

	public PacketPortalData() {
	}

	public PacketPortalData(Set<GlobalBlockPos> data) {
		super();
		this.data = data;
	}

	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		data = new Gson().fromJson(buf.readNBTTagCompoundFromBuffer().getString("data"), new TypeToken<Set<GlobalBlockPos>>() {
		}.getType());
		System.out.println("read:");
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("data", new Gson().toJson(data));
		buf.writeNBTTagCompoundToBuffer(nbt);
		System.out.println("write:");
	}

	@Override
	public void processPacket(INetHandlerPlayClient handler) {
//		System.out.println("handle");
		PortalData.get(Minecraft.getMinecraft().theWorld).valids = data;
	}

}
