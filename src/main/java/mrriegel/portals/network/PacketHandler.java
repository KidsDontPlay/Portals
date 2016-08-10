package mrriegel.portals.network;

import mrriegel.portals.Portals;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = new SimpleNetworkWrapper(Portals.MODID);

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(MessageName.Handler.class, MessageName.class, id++, Side.SERVER);
		INSTANCE.registerMessage(MessageButton.Handler.class, MessageButton.class, id++, Side.SERVER);
		INSTANCE.registerMessage(MessageData.Handler.class, MessageData.class, id++, Side.CLIENT);

	}
}
