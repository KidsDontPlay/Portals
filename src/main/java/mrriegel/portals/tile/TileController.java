package mrriegel.portals.tile;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import mrriegel.portals.init.ModBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class TileController extends TileBase {

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private EnumFacing face;
	private int index;

	public void refresh() {

	}

	public static int max = 50;

	public void scanFrame() {
		Set<MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>>> sets = Sets.newHashSet();
		// EnumFacing f=EnumFacing.UP;
		for (EnumFacing f : EnumFacing.VALUES) {
			for (int i = 0; i < 2; i++) {
				Set<BlockPos> portals = Sets.newHashSet();
				if (!worldObj.isAirBlock(pos.offset(f)))
					continue;
				portals.add(pos.offset(f));
				// addPortals(pos.offset(f), portals, valids(f, i));
				boolean succes = addPortals(pos.offset(f), portals, valids(f, i));
				// System.out.println(succes + " " + f + " " + i);
				if (succes)
					sets.add(MutablePair.of(portals, MutablePair.of(f, i)));

			}
		}

		MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>> fin = null;
		for (MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>> pa : sets) {
			if (fin == null || pa.getLeft().size() > fin.getLeft().size())
				fin = MutablePair.of(pa.getLeft(), pa.getRight());
		}
		if (fin != null)
			System.out.println(fin.getLeft().size() + " siii");
		portals = fin != null && fin.getLeft() != null ? Sets.newHashSet(fin.getLeft()) : Sets.<BlockPos> newHashSet();
		face = fin != null && fin.getRight() != null ? fin.getRight().getLeft() : null;
		index = fin != null && fin.getRight() != null ? fin.getRight().getRight() : -1;
		for (BlockPos p : portals)
			worldObj.setBlockState(p, Blocks.GOLD_BLOCK.getDefaultState());
		frames = Sets.newHashSet();
		for (BlockPos p : portals) {
			for (EnumFacing face : EnumFacing.VALUES) {
				BlockPos pp = p.offset(face);
				if (worldObj.getTileEntity(pp) instanceof TileFrame)
					frames.add(pp);
			}
		}

	}

	private boolean addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals.size() >= max) {
			return false;
		}
		for (EnumFacing face : faces) {
			BlockPos bl = pos.offset(face);
			Chunk chunk = worldObj.getChunkFromBlockCoords(bl);
			if (chunk == null || !chunk.isLoaded()) {
				System.out.println("zwei_ " + worldObj.getBlockState(bl));
				return false;
			}
			if (worldObj.isAirBlock(bl) && !portals.contains(bl)) {
				if (validNeighbors(bl, faces)) {
					portals.add(bl);
					addPortals(bl, portals, faces);
				} else
					return false;
			}
			if (!worldObj.isAirBlock(bl) && !validFrame(bl)) {
				System.out.println("vier_ " + worldObj.getBlockState(bl));
				return false;
			}
		}
		return true;

	}

	private boolean validFrame(BlockPos p) {
		boolean frame = worldObj.getBlockState(p).getBlock() == ModBlocks.frame;
		boolean cont = p.equals(this.pos);
		return frame || cont;
	}

	private boolean validNeighbor(BlockPos p) {
		return validFrame(p) || worldObj.isAirBlock(p);
	}

	private boolean validNeighbors(BlockPos p, Set<EnumFacing> set) {
		for (EnumFacing f : set)
			if (!validNeighbor(p.offset(f)))
				return false;
		return true;
	}

	private Set<EnumFacing> valids(EnumFacing face, int index) {
		Set<EnumFacing> set = Sets.newHashSet();
		set.add(face);
		set.add(face.getOpposite());
		switch (face) {
		case DOWN:
			if (index == 0) {
				set.add(EnumFacing.EAST);
				set.add(EnumFacing.WEST);
			} else {
				set.add(EnumFacing.NORTH);
				set.add(EnumFacing.SOUTH);
			}
			break;
		case EAST:
			if (index == 0) {
				set.add(EnumFacing.DOWN);
				set.add(EnumFacing.UP);
			} else {
				set.add(EnumFacing.NORTH);
				set.add(EnumFacing.SOUTH);
			}
			break;
		case NORTH:
			if (index == 0) {
				set.add(EnumFacing.DOWN);
				set.add(EnumFacing.UP);
			} else {
				set.add(EnumFacing.EAST);
				set.add(EnumFacing.WEST);
			}
			break;
		case SOUTH:
			if (index == 0) {
				set.add(EnumFacing.DOWN);
				set.add(EnumFacing.UP);
			} else {
				set.add(EnumFacing.EAST);
				set.add(EnumFacing.WEST);
			}
			break;
		case UP:
			if (index == 0) {
				set.add(EnumFacing.EAST);
				set.add(EnumFacing.WEST);
			} else {
				set.add(EnumFacing.NORTH);
				set.add(EnumFacing.SOUTH);
			}
			break;
		case WEST:
			if (index == 0) {
				set.add(EnumFacing.DOWN);
				set.add(EnumFacing.UP);
			} else {
				set.add(EnumFacing.NORTH);
				set.add(EnumFacing.SOUTH);
			}
			break;

		}
		return set;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		frames = new Gson().fromJson(compound.getString("frames"), new TypeToken<Set<BlockPos>>() {
		}.getType());
		portals = new Gson().fromJson(compound.getString("portals"), new TypeToken<Set<BlockPos>>() {
		}.getType());
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("frames", new Gson().toJson(frames));
		compound.setString("portals", new Gson().toJson(portals));
		return super.writeToNBT(compound);
	}
}
