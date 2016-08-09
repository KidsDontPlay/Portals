package mrriegel.portals.tile;

import java.math.BigInteger;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import mrriegel.portals.PortalData;
import mrriegel.portals.PortalData.GlobalBlockPos;
import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class TileController extends TileBase implements IPortalFrame {

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private ItemStack[] stacks = new ItemStack[8];
	// private EnumFacing face;
	// private int index;
	private boolean privat, active, valid;
	private String owner, name = RandomStringUtils.random(10, true, true);
	private GlobalBlockPos target;
	private BlockPos selfLanding;
	private int colorPortal = 0xffffff, colorParticle = 0xffffff;
	private EnumFacing looking = EnumFacing.NORTH;

	public static int max = 300;

	public boolean validatePortal() {
		Set<EnumFacing> faces = Sets.newHashSet();
		Set<BlockPos> set = getPortalBlocks(faces);
		if (frameChanged(set)) {
			valid = !set.isEmpty();
			if (!valid && active)
				deactivate();
			portals = Sets.newHashSet(set);
			if (!faces.isEmpty())
				addFrames(faces);
		}
		if (valid) {
			PortalData.get(worldObj).add(new GlobalBlockPos(pos, worldObj));
			calculateLanding();
		} else {
			PortalData.get(worldObj).remove(new GlobalBlockPos(pos, worldObj));
			selfLanding = null;
		}
		markDirty();
		return valid;
	}

	public void activate(@Nullable EntityPlayer player) {
		if (!validatePortal()) {
			if (player != null)
				player.addChatMessage(new TextComponentString("Invalid Portal Structure"));
			return;
		}
//		if (target == null)
//			return;
		Axis a = null;
		Set<Integer> x = Sets.newHashSet(), y = Sets.newHashSet(), z = Sets.newHashSet();
		for (BlockPos p : frames) {
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
			worldObj.getTileEntity(p).markDirty();
		}
		active = true;
		markDirty();

	}

	public void deactivate() {
		for (BlockPos p : portals)
			if (worldObj.getBlockState(p).getBlock() == ModBlocks.portaal) {
				worldObj.setBlockToAir(p);
			}
		for (BlockPos p : frames)
			if (worldObj.getTileEntity(p) instanceof TileFrame) {
				((TileFrame) worldObj.getTileEntity(p)).setController(null);
			}
		active = false;
		markDirty();

	}

	private void calculateLanding() {
		Set<BlockPos> selfs = Sets.newHashSet();
		for (BlockPos p : portals) {
			IBlockState a = worldObj.getBlockState(p);
			IBlockState aPlus = worldObj.getBlockState(p.up());
			IBlockState aMinus = worldObj.getBlockState(p.down());
			if (a.getBlock().getCollisionBoundingBox(a, worldObj, p) == null && aPlus.getBlock().getCollisionBoundingBox(aPlus, worldObj, p.up()) == null && !portals.contains(p.down())) {
				selfs.add(p);
			}
		}
		if (selfs.isEmpty())
			selfLanding = null;
		else
			selfLanding = selfs.iterator().next();
	}

	boolean frameChanged(Set<BlockPos> set) {
		if (portals == null)
			return true;
		return !(portals.containsAll(set) && set.containsAll(portals));
	}

	private boolean isPortalActive(GlobalBlockPos pos) {
		boolean valid = PortalData.get(pos.getWorld()).validPos(pos.getWorld(), pos.getPos());
		if (!valid)
			return false;
		if (pos.getTile() instanceof TileController && ((TileController) pos.getTile()).isActive())
			return true;
		for (EnumFacing face : EnumFacing.VALUES) {
			if (pos.getWorld().getBlockState(pos.getPos().offset(face)).getBlock() == ModBlocks.portaal)
				return true;
		}
		return false;
	}

	public Set<BlockPos> getPortalBlocks(Set<EnumFacing> faces) {
		Set<BlockPos> ps = Sets.newHashSet();
		Set<MutablePair<Set<BlockPos>, MutablePair<EnumFacing, Integer>>> sets = Sets.newHashSet();
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
		if (fin != null && fin.getRight() != null)
			faces.addAll(valids(fin.getRight().getLeft(), fin.getRight().getRight()));
		return ps;
	}

	public void addFrames(Set<EnumFacing> faces) {
		frames = Sets.newHashSet();
		for (BlockPos p : portals) {
			for (EnumFacing face : faces) {
				BlockPos pp = p.offset(face);
				if (worldObj.getTileEntity(pp) instanceof TileFrame) {
					frames.add(pp);
					((TileFrame) worldObj.getTileEntity(pp)).setController(this.pos);
					worldObj.getTileEntity(pp).markDirty();
				}
			}
		}
	}

	private void addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals.size() > max)
			return;
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

	private boolean validNeighbors(BlockPos p, Set<EnumFacing> faces) {
		for (EnumFacing f : faces)
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

	public Set<Upgrade> getUpgrades() {
		Set<Upgrade> set = Sets.newHashSet();
		for (ItemStack stack : stacks) {
			if (stack != null) {
				set.add(Upgrade.values()[stack.getItemDamage()]);
			}
		}
		return set;
	}

	public void teleport(Entity entity) {
		if (entity == null || !valid || !active || !isPortalActive(target))
			return;
		TileController tar = (TileController) target.getWorld().getTileEntity(target.getPos());
		if (tar == null || tar.selfLanding == null)
			return;
		Vec3d before = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
		if (entity.worldObj.provider.getDimension() == target.getDimension()) {
			entity.setPositionAndUpdate(tar.getSelfLanding().getX() + .5, tar.getSelfLanding().getY() + .05, tar.getSelfLanding().getZ() + .5);
		} else {
			// entity.changeDimension(target.getDimension());
			changeDimension(entity, target);
			// entity.setPositionAndUpdate(tar.getSelfLanding().getX() + .5,
			// tar.getSelfLanding().getY() + .05, tar.getSelfLanding().getZ() +
			// .5);
		}
		if (tar.getUpgrades().contains(Upgrade.DIRECTION) && tar.looking != null) {
			entity.setRotationYawHead(tar.looking.getHorizontalAngle());
		}
		if (tar.getUpgrades().contains(Upgrade.MOTION)) {
			entity.motionX = before.xCoord;
			entity.motionY = before.yCoord;
			entity.motionZ = before.zCoord;
		} else {
			entity.motionX = 0;
			entity.motionY = 0;
			entity.motionZ = 0;
		}
		if (entity instanceof EntityPlayerMP)
			((EntityPlayerMP) entity).connection.netManager.sendPacket(new SPacketEntityVelocity(entity));

	}

	private void changeDimension(Entity entityTP, GlobalBlockPos pos) {
		TileController tar = (TileController) target.getWorld().getTileEntity(target.getPos());
		if (!entityTP.worldObj.isRemote && !entityTP.isDead && tar != null) {
			int dimensionIn = pos.getDimension();
			if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(entityTP, dimensionIn))
				return;
			entityTP.worldObj.theProfiler.startSection("changeDimension");
			MinecraftServer minecraftserver = entityTP.getServer();
			int i = entityTP.dimension;
			WorldServer currentServer = minecraftserver.worldServerForDimension(i);
			WorldServer targetServer = minecraftserver.worldServerForDimension(dimensionIn);
			entityTP.dimension = dimensionIn;

			// if (i == 1 && dimensionIn == 1) {
			// targetServer = minecraftserver.worldServerForDimension(0);
			// entityTP.dimension = 0;
			// }

			entityTP.worldObj.removeEntity(entityTP);
			entityTP.isDead = false;
			entityTP.worldObj.theProfiler.startSection("reposition");
			// BlockPos blockpos;
			//
			// if (dimensionIn == 1) {
			// blockpos = targetServer.getSpawnCoordinate();
			// } else {
			// double d0 = entityTP.posX;
			// double d1 = entityTP.posZ;
			// double d2 = 8.0D;
			//
			// if (dimensionIn == -1) {
			// d0 = MathHelper.clamp_double(d0 / 8.0D,
			// targetServer.getWorldBorder().minX() + 16.0D,
			// targetServer.getWorldBorder().maxX() - 16.0D);
			// d1 = MathHelper.clamp_double(d1 / 8.0D,
			// targetServer.getWorldBorder().minZ() + 16.0D,
			// targetServer.getWorldBorder().maxZ() - 16.0D);
			// } else if (dimensionIn == 0) {
			// d0 = MathHelper.clamp_double(d0 * 8.0D,
			// targetServer.getWorldBorder().minX() + 16.0D,
			// targetServer.getWorldBorder().maxX() - 16.0D);
			// d1 = MathHelper.clamp_double(d1 * 8.0D,
			// targetServer.getWorldBorder().minZ() + 16.0D,
			// targetServer.getWorldBorder().maxZ() - 16.0D);
			// }
			//
			// d0 = (double) MathHelper.clamp_int((int) d0, -29999872,
			// 29999872);
			// d1 = (double) MathHelper.clamp_int((int) d1, -29999872,
			// 29999872);
			// float f = entityTP.rotationYaw;
			// entityTP.setLocationAndAngles(d0, entityTP.posY, d1, 90.0F,
			// 0.0F);
			// Teleporter teleporter = targetServer.getDefaultTeleporter();
			// teleporter.placeInExistingPortal(entityTP, f);
			// blockpos = new BlockPos(entityTP);
			// }

			currentServer.updateEntityWithOptionalForce(entityTP, false);
			entityTP.worldObj.theProfiler.endStartSection("reloading");
			Entity entity = EntityList.createEntityByName(EntityList.getEntityString(entityTP), targetServer);

			if (entity != null) {
				// entity.copyDataFromOld(entity);

				// if (i == 1 && dimensionIn == 1) {
				// BlockPos blockpos1 =
				// targetServer.getTopSolidOrLiquidBlock(targetServer.getSpawnPoint());
				// entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw,
				// entity.rotationPitch);
				// } else {
				// entity.moveToBlockPosAndAngles(blockpos, entity.rotationYaw,
				// entity.rotationPitch);
				// }
				entity.setPositionAndUpdate(tar.getSelfLanding().getX() + .5, tar.getSelfLanding().getY() + .05, tar.getSelfLanding().getZ() + .5);

				boolean flag = entity.forceSpawn;
				entity.forceSpawn = true;
				targetServer.spawnEntityInWorld(entity);
				entity.forceSpawn = flag;
				targetServer.updateEntityWithOptionalForce(entity, false);
			}

			entityTP.isDead = true;
			entityTP.worldObj.theProfiler.endSection();
			currentServer.resetUpdateEntityTick();
			targetServer.resetUpdateEntityTick();
			entityTP.worldObj.theProfiler.endSection();
		}
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
		if (compound.hasKey("name"))
			name = compound.getString("name");
		else
			name = null;
		target = GlobalBlockPos.loadGlobalPosFromNBT(compound);
		if (compound.hasKey("selfLanding"))
			selfLanding = BlockPos.fromLong(compound.getLong("selfLanding"));
		else
			selfLanding = null;
		if (compound.hasKey("looking"))
			looking = EnumFacing.byName(compound.getString("looking"));
		else
			looking = null;
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
		this.stacks = new ItemStack[stacks.length];
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;
			if (j >= 0 && j < this.stacks.length) {
				this.stacks[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
		colorParticle = compound.getInteger("colorParticle");
		colorPortal = compound.getInteger("colorPortal");
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
		if (name != null)
			compound.setString("name", name);
		if (target != null)
			target.writeToNBT(compound);
		if (selfLanding != null)
			compound.setLong("selfLanding", selfLanding.toLong());
		if (looking != null)
			compound.setString("looking", looking.getName2());
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
		compound.setInteger("colorParticle", colorParticle);
		compound.setInteger("colorPortal", colorPortal);

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GlobalBlockPos getTarget() {
		return target;
	}

	public void setTarget(GlobalBlockPos target) {
		this.target = target;
	}

	public BlockPos getSelfLanding() {
		return selfLanding;
	}

	public void setSelfLanding(BlockPos selfLanding) {
		this.selfLanding = selfLanding;
	}

	public ItemStack[] getStacks() {
		return stacks;
	}

	public void setStacks(ItemStack[] stacks) {
		this.stacks = stacks;
	}

	public int getColorPortal() {
		return colorPortal;
	}

	public void setColorPortal(int colorPortal) {
		this.colorPortal = colorPortal;
	}

	public int getColorParticle() {
		return colorParticle;
	}

	public void setColorParticle(int colorParticle) {
		this.colorParticle = colorParticle;
	}

	private static class PortalException extends RuntimeException {
		public PortalException(String msg) {
			super(msg);
		}
	}

	@Override
	public TileController getTileController() {
		return this;
	}
}
