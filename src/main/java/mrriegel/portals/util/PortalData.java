package mrriegel.portals.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.PacketHandler;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.network.DataMessage;
import mrriegel.portals.tile.TileController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class PortalData extends WorldSavedData {

	public Set<GlobalBlockPos> valids = Sets.newHashSet();
	private static final String DATA_NAME = "PortaalData";

	public PortalData() {
		super(DATA_NAME);
	}

	public PortalData(String s) {
		super(s);
	}

	public static PortalData get(World world) {
		return get(world, DATA_NAME);
	}

	public static PortalData get(World world, String name) {
		MapStorage storage = world.getMapStorage();
		PortalData instance = (PortalData) storage.getOrLoadData(PortalData.class, name);
		if (instance == null) {
			instance = new PortalData();
			storage.setData(name, instance);
		}
		instance.validate();
		return instance;
	}

	public static void sync(@Nullable EntityPlayer player) {
		NBTTagCompound nbt = new NBTTagCompound();
		if (player instanceof EntityPlayerMP) {
			NBTHelper.setString(nbt, "data", new Gson().toJson(PortalData.get(player.world).valids));
			PacketHandler.sendTo(new DataMessage(nbt), (EntityPlayerMP) player);
		} else
			for (EntityPlayer p : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
				NBTHelper.setString(nbt, "data", new Gson().toJson(PortalData.get(p.world).valids));
				PacketHandler.sendTo(new DataMessage(nbt), (EntityPlayerMP) p);
			}
	}

	public void add(GlobalBlockPos pos) {
		valids.add(pos);
		sync(null);
		markDirty();
	}

	public void remove(GlobalBlockPos pos) {
		valids.remove(pos);
		sync(null);
		markDirty();
	}

	public boolean validPos(World world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock() != ModBlocks.controller) {
			remove(new GlobalBlockPos(pos, world));
			return false;
		}
		return valids.contains(new GlobalBlockPos(pos, world));
	}

	public List<String> getNames() {
		List<String> lis = Lists.newArrayList();
		for (GlobalBlockPos p : valids) {
			if (validPos(p.getWorld(), p.getPos()))
				lis.add(((TileController) p.getTile()).getName());
		}
		return lis;
	}

	public TileController getTile(String name) {
		for (GlobalBlockPos p : valids) {
			if (validPos(p.getWorld(), p.getPos()) && ((TileController) p.getTile()).getName().equals(name))
				return (TileController) p.getTile();
		}
		return null;
	}

	public boolean nameOccupied(String name, GlobalBlockPos p) {
		for (GlobalBlockPos pos : valids) {
			if (pos.getTile() instanceof TileController && !pos.equals(p)) {
				if (((TileController) pos.getTile()).getName().equals(name))
					return true;
			}
		}
		return false;
	}

	@SuppressWarnings("serial")
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		valids = new Gson().fromJson(nbt.getString("valids"), new TypeToken<Set<GlobalBlockPos>>() {
		}.getType());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setString("valids", new Gson().toJson(valids));
		return nbt;
	}

	public void validate() {
		valids.removeAll(Collections.singleton(null));
		Iterator<GlobalBlockPos> it = valids.iterator();
		while (it.hasNext()) {
			GlobalBlockPos p = it.next();
			if (p.getPos() == null /**
			 * || !(p.getTile() instanceof
			 * TileController)
			 */
			)
				it.remove();
		}
		markDirty();
	}

}
