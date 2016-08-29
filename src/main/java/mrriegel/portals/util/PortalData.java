package mrriegel.portals.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.network.MessageData;
import mrriegel.portals.network.PacketHandler;
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

	public void add(GlobalBlockPos pos) {
		valids.add(pos);
		World w = pos.getWorld(null);
		for (EntityPlayer p : w != null ? w.playerEntities : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList())
			PacketHandler.INSTANCE.sendTo(new MessageData(valids), (EntityPlayerMP) p);
		markDirty();
	}

	public void remove(GlobalBlockPos pos) {
		valids.remove(pos);
		World w = pos.getWorld(null);
		for (EntityPlayer p : w != null ? w.playerEntities : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerList())
			PacketHandler.INSTANCE.sendTo(new MessageData(valids), (EntityPlayerMP) p);
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
			if (validPos(p.getWorld(null), p.getPos()))
				lis.add(((TileController) p.getTile(null)).getName());
		}
		return lis;
	}

	public TileController getTile(String name) {
		for (GlobalBlockPos p : valids) {
			if (validPos(p.getWorld(null), p.getPos()) && ((TileController) p.getTile(null)).getName().equals(name))
				return (TileController) p.getTile(null);
		}
		return null;
	}

	public boolean nameOccupied(String name, GlobalBlockPos p) {
		for (GlobalBlockPos pos : valids) {
			if (pos.getTile(null) instanceof TileController && !pos.equals(p)) {
				if (((TileController) pos.getTile(null)).getName().equals(name))
					return true;
			}
		}
		return false;
	}

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
	
	public void validate(){
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
