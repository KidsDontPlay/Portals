package mrriegel.portals.tile;

import java.util.Set;

import javax.annotation.Nullable;

import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.init.ModBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class TileController extends TileBase {

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private ItemStack[] stacks = new ItemStack[8];
	// private EnumFacing face;
	// private int index;
	private boolean privat, active, valid;
	private String owner;
	private BlockPos target;

	public static int max = 300;

	public boolean validatePortal() {
		Set<BlockPos> set = getPortalBlocks();
		if (frameChanged(set)) {
			valid = !set.isEmpty();
			if (!valid && active)
				deactivate();
			portals = Sets.newHashSet(set);
			addFrames();
		}
		return valid;
	}

	public void activate(@Nullable EntityPlayer player) {
		if (!validatePortal()) {
			if (player != null)
				player.addChatMessage(new TextComponentString("Invalid Portal Structure"));
			return;
		}
		Axis a = null;
		Set<Integer> x = Sets.newHashSet(), y = Sets.newHashSet(), z = Sets.newHashSet();
		for (BlockPos p : portals) {
			x.add(p.getX());
			y.add(p.getY());
			z.add(p.getZ());
		}
		if (x.size() == 1)
			a = Axis.Z;
		else if (y.size() == 1)
			a = Axis.Y;
		else if (z.size() == 1)
			a = Axis.X;
		for (BlockPos p : portals) {
			worldObj.setBlockState(p, ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS, a));
			((TilePortaal) worldObj.getTileEntity(p)).setController(this.pos);
		}
		active = true;

	}

	public void deactivate() {
		System.out.println("deactvi");
		for (BlockPos p : portals)
			if (worldObj.getBlockState(p).getBlock() == ModBlocks.portaal) {
				worldObj.setBlockToAir(p);
				System.out.println("new air");
			}
		active = false;

	}

	boolean frameChanged(Set<BlockPos> set) {
		if (portals == null)
			return true;
		return !(portals.containsAll(set) && set.containsAll(portals));
	}

	public Set<BlockPos> getPortalBlocks() {
		Set<BlockPos> ps = Sets.newHashSet();
		Set<MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>>> sets = Sets.newHashSet();
		// EnumFacing f=EnumFacing.UP;
		for (EnumFacing f : EnumFacing.VALUES) {
			for (int i = 0; i < 2; i++) {
				Set<BlockPos> portals = Sets.newHashSet();
				if (!validPortalPos(pos.offset(f)))
					continue;
				portals.add(pos.offset(f));
				try {
					addPortals(pos.offset(f), portals, valids(f, i));
					if (portals.size() <= max && validNeighbors(portals, valids(f, i)))
						sets.add(MutablePair.of(portals, MutablePair.of(f, i)));
				} catch (PortalException e) {
					// Portals.logger.error(e.getLocalizedMessage());
				}
			}
		}

		MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>> fin = null;
		for (MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>> pa : sets) {
			if (fin == null || pa.getLeft().size() > fin.getLeft().size())
				fin = MutablePair.of(pa.getLeft(), pa.getRight());
		}
		ps = fin != null && fin.getLeft() != null ? Sets.newHashSet(fin.getLeft()) : Sets.<BlockPos> newHashSet();
		// face = fin != null && fin.getRight() != null ?
		// fin.getRight().getLeft() : null;
		// index = fin != null && fin.getRight() != null ?
		// fin.getRight().getRight() : -1;
		return ps;
	}

	public void addFrames() {
		frames = Sets.newHashSet();
		for (BlockPos p : portals) {
			for (EnumFacing face : EnumFacing.VALUES) {
				BlockPos pp = p.offset(face);
				if (worldObj.getTileEntity(pp) instanceof TileFrame) {
					frames.add(pp);
					((TileFrame) worldObj.getTileEntity(pp)).setController(this.pos);
				}
			}
		}
	}

	private void addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals.size() > max) {
			return;
		}
		for (EnumFacing face : faces) {
			BlockPos bl = pos.offset(face);
			Chunk chunk = worldObj.getChunkFromBlockCoords(bl);
			if (chunk == null || !chunk.isLoaded()) {
				throw new PortalException("Chunk not loaded.");
			}
			if (validPortalPos(bl) && !portals.contains(bl)) {
				if (validNeighbors(bl, faces)) {
					portals.add(bl);
					addPortals(bl, portals, faces);
				} else
					throw new PortalException("Invalid Frame Blocks.");
			}
			// if (!worldObj.isAirBlock(bl) && !validFrame(bl)) {
			// throw new ChunkException("Chunk not loaded.");
			// }
		}

	}

	private boolean validPortalPos(BlockPos p) {
		return worldObj.isAirBlock(p) || worldObj.getBlockState(p).getBlock() == ModBlocks.portaal;
	}

	private boolean validFrame(BlockPos p) {
		return worldObj.getBlockState(p).getBlock() == ModBlocks.frame || p.equals(this.pos);
	}

	private boolean validNeighbor(BlockPos p) {
		return validFrame(p) || validPortalPos(p);
	}

	private boolean validNeighbors(BlockPos p, Set<EnumFacing> set) {
		for (EnumFacing f : set)
			if (!validNeighbor(p.offset(f)))
				return false;
		return true;
	}

	private boolean validNeighbors(Set<BlockPos> set, Set<EnumFacing> faces) {
		for (BlockPos p : set)
			if (!validNeighbors(p, faces))
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
		active = compound.getBoolean("active");
		privat = compound.getBoolean("privat");
		valid = compound.getBoolean("valid");
		if (compound.hasKey("owner"))
			owner = compound.getString("owner");
		else
			owner = null;
		if (compound.hasKey("target"))
			target = BlockPos.fromLong(compound.getLong("target"));
		else
			target = null;
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		this.stacks = new ItemStack[stacks.length];
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			if (j >= 0 && j < this.stacks.length) {
				this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("frames", new Gson().toJson(frames));
		compound.setString("portals", new Gson().toJson(portals));
		compound.setBoolean("active", active);
		compound.setBoolean("privat", privat);
		compound.setBoolean("valid", valid);
		if (owner != null)
			compound.setString("owner", owner);
		if (target != null)
			compound.setLong("target", target.toLong());
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < this.stacks.length; ++i) {
			if (this.stacks[i] != null) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				this.stacks[i].writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		compound.setTag("Items", nbttaglist);

		return super.writeToNBT(compound);
	}

	public Set<BlockPos> getFrames() {
		return frames;
	}

	public void setFrames(Set<BlockPos> frames) {
		this.frames = frames;
	}

	public Set<BlockPos> getPortals() {
		return portals;
	}

	public void setPortals(Set<BlockPos> portals) {
		this.portals = portals;
	}

	public boolean isPrivat() {
		return privat;
	}

	public void setPrivat(boolean privat) {
		this.privat = privat;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public BlockPos getTarget() {
		return target;
	}

	public void setTarget(BlockPos target) {
		this.target = target;
	}

	public ItemStack[] getStacks() {
		return stacks;
	}

	public void setStacks(ItemStack[] stacks) {
		this.stacks = stacks;
	}

	private static class PortalException extends RuntimeException {
		public PortalException(String msg) {
			super(msg);
		}
	}
}
