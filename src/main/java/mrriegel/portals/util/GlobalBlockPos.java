package mrriegel.portals.util;

import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class GlobalBlockPos {
	private BlockPos pos;
	private int dimension;

	public GlobalBlockPos(BlockPos pos, int dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}

	public GlobalBlockPos(BlockPos pos, World world) {
		this(pos, world.provider.getDimension());
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

	public World getWorld(@Nullable World world) {
		if (world != null && world.provider.getDimension() == dimension)
			return world;
		return (world != null && world.getMinecraftServer() != null ? world.getMinecraftServer() : FMLCommonHandler.instance().getMinecraftServerInstance()).worldServerForDimension(dimension);
	}

	public TileEntity getTile(@Nullable World world) {
		return getWorld(world).getTileEntity(getPos());
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
