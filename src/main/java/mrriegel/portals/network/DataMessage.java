package mrriegel.portals.network;

import java.util.Set;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.AbstractMessage;
import mrriegel.limelib.network.WorldDataMessage;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.util.PortalData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class DataMessage extends AbstractMessage<DataMessage> {

	public DataMessage() {
	}

	public DataMessage(NBTTagCompound nbt) {
		super(nbt);
	}

	@Override
	public void handleMessage(EntityPlayer arg0, NBTTagCompound arg1, Side arg2) {
		PortalData.get(arg0.worldObj).valids = new Gson().fromJson(NBTHelper.getString(arg1, "data"), new TypeToken<Set<GlobalBlockPos>>() {
		}.getType());
		WorldDataMessage f;
	}
}
