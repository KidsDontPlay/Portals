package mrriegel.portals.tile;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import mrriegel.portals.init.ModBlocks;
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

	public void refresh() {

	}

	public void scanFrame() {
		// Map<EnumFacing, MutablePair<Set<BlockPos>, Set<BlockPos>>> sets =
		// Maps.newHashMap();
		Set<Set<BlockPos>> sets = Sets.newHashSet();
		for (EnumFacing f : EnumFacing.VALUES) {
			for (int i = 0; i < 2; i++) {
				Set<BlockPos> portals = Sets.newHashSet();
				portals.add(pos.offset(f));
				addPortals(pos.offset(f), portals, valids(f, i));
				sets.add(portals);
			}
		}
		sets.removeAll(Collections.singleton(null));
		Set<BlockPos> fin = null;
		for (Set<BlockPos> set : sets)
			if (fin == null || set.size() > fin.size())
				fin = Sets.newHashSet(set);
		portals = fin != null ? Sets.newHashSet(fin) : Sets.<BlockPos>newHashSet();
		frames = Sets.newHashSet();
		for (BlockPos p : portals) {
			for (EnumFacing f : EnumFacing.VALUES) {
				BlockPos pp = p.offset(f);
				if (worldObj.getTileEntity(pp) instanceof TileFrame)
					frames.add(pp);
			}
		}

	}

	private void addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals == null || portals.size() >= 400) {
			portals = null;
			return;
		}
		for (EnumFacing face : faces) {
			BlockPos bl = pos.offset(face);
			Chunk chunk = worldObj.getChunkFromBlockCoords(bl);
			if (chunk == null || !chunk.isLoaded()) {
				portals = null;
				return;
			}
			if (worldObj.isAirBlock(bl) && !portals.contains(bl)) {
				portals.add(bl);
				addPortals(bl, portals, faces);
			} else if (worldObj.getBlockState(bl).getBlock() != ModBlocks.frame) {
				portals = null;
				return;
			}
		}

	}

	private void addFrames(final BlockPos pos) {
		if (pos == null || worldObj == null)
			return;
		for (EnumFacing f : EnumFacing.VALUES) {
			BlockPos bl = pos.offset(f);
			Chunk chunk = worldObj.getChunkFromBlockCoords(bl);
			if (chunk == null || !chunk.isLoaded())
				continue;
			if (worldObj.getTileEntity(bl) != null && worldObj.getTileEntity(bl) instanceof TileController && !bl.equals(this.pos)) {
				return;
			}
			if (worldObj.getTileEntity(bl) != null && worldObj.getTileEntity(bl) instanceof TileFrame && !frames.contains(bl)) {
				frames.add(bl);
				((TileFrame) worldObj.getTileEntity(bl)).setController(this.pos);
				chunk.setChunkModified();
				addFrames(bl);
			}
		}
	}

	private Set<EnumFacing> valids(EnumFacing face,int index) {
		Set<EnumFacing> set = Sets.newHashSet();
		set.add(face);
		set.add(face.getOpposite());
		if(index==0){
			set.add(face.rotateAround(face.getAxis().))
		}
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
