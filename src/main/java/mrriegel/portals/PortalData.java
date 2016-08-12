package mrriegel.portals;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.tile.TileController;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.INBTSerializable;
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
		MapStorage storage = world.getMapStorage();
		PortalData instance = (PortalData) storage.getOrLoadData(PortalData.class, DATA_NAME);
		if (instance == null) {
			instance = new PortalData();
			storage.setData(DATA_NAME, instance);
		}
		instance.valids.removeAll(Collections.singleton(null));
		Iterator<GlobalBlockPos> it = instance.valids.iterator();
		while (it.hasNext()) {
			GlobalBlockPos p = it.next();
			if (p.pos == null /** || !(p.getTile() instanceof TileController) */
			)
				it.remove();
		}
		return instance;
	}

	public void add(GlobalBlockPos pos) {
		valids.add(pos);
		markDirty();
	}

	public void remove(GlobalBlockPos pos) {
		valids.remove(pos);
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

	public static class GlobalBlockPos {
		private BlockPos pos;
		private int dimension;

		public GlobalBlockPos(BlockPos pos, int dimension) {
			super();
			this.pos = pos;
			this.dimension = dimension;
		}

		public GlobalBlockPos(BlockPos pos, World world) {
			super();
			this.pos = pos;
			this.dimension = world.provider.getDimension();
		}

		private GlobalBlockPos() {
		}

		@Override
		public String toString() {
			return "GlobalBlockPos [pos=" + pos + ", dimension=" + dimension + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dimension;
			result = prime * result + ((pos == null) ? 0 : pos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GlobalBlockPos other = (GlobalBlockPos) obj;
			if (dimension != other.dimension)
				return false;
			if (pos == null) {
				if (other.pos != null)
					return false;
			} else if (!pos.equals(other.pos))
				return false;
			return true;
		}

		public BlockPos getPos() {
			return pos;
		}

		public void setPos(BlockPos pos) {
			this.pos = pos;
		}

		public int getDimension() {
			return dimension;
		}

		public void setDimension(int dimension) {
			this.dimension = dimension;
		}

		public World getWorld() {
			return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension);
		}

		public TileEntity getTile() {
			return getWorld().getTileEntity(getPos());
		}

		public void readFromNBT(NBTTagCompound compound) {
			if (compound.hasKey("Gpos"))
				pos = BlockPos.fromLong(compound.getLong("Gpos"));
			else
				pos = null;
			dimension = compound.getInteger("Gdim");
		}

		public void writeToNBT(NBTTagCompound compound) {
			if (pos != null)
				compound.setLong("Gpos", pos.toLong());
			compound.setInteger("Gdim", dimension);
		}

		public static GlobalBlockPos loadGlobalPosFromNBT(NBTTagCompound nbt) {
			GlobalBlockPos pos = new GlobalBlockPos();
			pos.readFromNBT(nbt);
			return pos.getPos() != null ? pos : null;
		}

	}

}
