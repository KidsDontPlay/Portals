package mrriegel.portals.tile;

import java.util.List;
import java.util.Set;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.TeleportationHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.Utils;
import mrriegel.portals.Portals;
import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.util.PortalData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TileController extends CommonTile implements IPortalFrame, IEnergyReceiver {

	public static final int untilPort = 9;
	public static final int BUTTON = 0, NAME = 1, UPGRADE = 2;
	private static int max = 300;

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private ItemStack[] stacks = new ItemStack[8];
	private boolean privat, active, valid;
	private String owner, name = RandomStringUtils.random(10, true, true), targetName;
	private GlobalBlockPos target;
	private BlockPos selfLanding;
	private int colorPortal = 0x9500fe, colorParticle = 0x9500fe, colorFrame = 0x9500fe;
	private EnumFacing looking = EnumFacing.NORTH;

	public EnergyStorage en = new EnergyStorage(64000, 1000, 0);

	public boolean validatePortal() {
		Set<EnumFacing> faces = Sets.newHashSet();
		Set<BlockPos> set = getPortalBlocks(faces);
		if (frameChanged(set)) {
			valid = !set.isEmpty();
			if (!valid && active)
				deactivate();
			portals = Sets.newHashSet(set);
			// System.out.println("fac: "+faces.size());
			if (!faces.isEmpty())
				addFrames(faces);
		}
		if (valid) {
			PortalData.get(world).add(new GlobalBlockPos(pos, world));
			calculateLanding();
		} else {
			PortalData.get(world).remove(new GlobalBlockPos(pos, world));
			selfLanding = null;
		}
		sync();
		return valid;
	}

	public boolean activate() {
		if (!validatePortal()) {
			return false;
		}
		if (target == null)
			return false;
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
			world.setBlockState(p, ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS, a));
			((TilePortaal) world.getTileEntity(p)).setController(this.pos);
			((TilePortaal) world.getTileEntity(p)).markDirty();
		}
		active = true;
		sync();
		for (BlockPos p : frames)
			if (world.getTileEntity(p) instanceof TileFrame)
				((TileFrame) world.getTileEntity(p)).sync();
		return true;

	}

	public void deactivate() {
		for (BlockPos p : portals)
			if (world.getBlockState(p).getBlock() == ModBlocks.portaal) {
				world.setBlockToAir(p);
			}
		// for (BlockPos p : frames)
		// if (world.getTileEntity(p) instanceof TileFrame) {
		// ((TileFrame) world.getTileEntity(p)).setController(null);
		// ((TileFrame) world.getTileEntity(p)).sync();
		// }
		active = false;
		sync();
		for (BlockPos p : frames)
			if (world.getTileEntity(p) instanceof TileFrame)
				((TileFrame) world.getTileEntity(p)).sync();

	}

	private void calculateLanding() {
		Set<BlockPos> selfs = Sets.newHashSet();
		for (BlockPos p : portals) {
			IBlockState a = world.getBlockState(p);
			IBlockState aPlus = world.getBlockState(p.up());
//			IBlockState aMinus = world.getBlockState(p.down());
			if (a.getBlock().getCollisionBoundingBox(a, world, p) == null && aPlus.getBlock().getCollisionBoundingBox(aPlus, world, p.up()) == null && !portals.contains(p.down())) {
				selfs.add(p);
			}
		}
		if (selfs.isEmpty())
			selfLanding = null;
		else {
			selfLanding = selfs.iterator().next();
		}
	}

	boolean frameChanged(Set<BlockPos> set) {
		if (portals == null)
			return true;
		return !(portals.containsAll(set) && set.containsAll(portals));
	}

	public boolean isPortalActive(GlobalBlockPos pos) {
		if (pos == null)
			return false;
		boolean valid = PortalData.get(pos.getWorld()).validPos(pos.getWorld(), pos.getPos());
		if (!valid)
			return false;
		if (pos.getTile() instanceof TileController && ((TileController) pos.getTile()).isActive() // &&
		// ((TileController)
		// pos.getTile()).getTarget().equals(new
		// GlobalBlockPos(this.pos,
		// world))
		)
			return true;
		// for (EnumFacing face : EnumFacing.VALUES) {
		// if
		// (pos.getWorld(world).getBlockState(pos.getPos().offset(face)).getBlock()
		// == ModBlocks.portaal)
		// return true;
		// }
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
				if (world.getTileEntity(pp) instanceof TileFrame) {
					frames.add(pp);
					((TileFrame) world.getTileEntity(pp)).setController(this.pos);
					((TileFrame) world.getTileEntity(pp)).sync();
				}
			}
		}
	}

	private void addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals.size() > max)
			return;
		for (EnumFacing face : faces) {
			BlockPos bl = pos.offset(face);
			Chunk chunk = world.getChunkFromBlockCoords(bl);
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
			// if (!world.isAirBlock(bl) && !validFrame(bl)) {
			// throw new ChunkException("Chunk not loaded.");
			// }
		}

	}

	private boolean validPortalPos(BlockPos p) {
		return world.isAirBlock(p) || world.getBlockState(p).getBlock() == ModBlocks.portaal;
	}

	private boolean validFrame(BlockPos p) {
		return world.getBlockState(p).getBlock() == ModBlocks.frame || p.equals(this.pos);
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
		if (entity == null || !valid || !active || !isPortalActive(target) || entity.world.isRemote)
			return;
		TileController tar = (TileController) target.getTile();
		if (tar == null || tar.selfLanding == null)
			return;
		//		int oldDim = entity.world.provider.getDimension();
		//		if (oldDim == target.getDimension()) {
		//			System.out.println("try");
		//			TeleportationHelper.teleportToPosAndUpdate(entity, tar.getSelfLanding());
		//		} else {
		entity.getEntityData().setBoolean("ported", true);
		TeleportationHelper.serverTeleport(entity, tar.getSelfLanding(), target.getDimension());
		//		}
		if (tar.getUpgrades().contains(Upgrade.DIRECTION) && tar.looking != null) {
			if (entity instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) entity;
				player.rotationYaw = tar.looking.getHorizontalAngle();
				player.connection.sendPacket(new SPacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, Sets.<SPacketPlayerPosLook.EnumFlags> newHashSet(), 1000));
			}
		}
		//		boolean player = false;
		//		for (Entity e : tar.world.loadedEntityList)
		//			if (e instanceof EntityPlayer) {
		//				player = true;
		//				break;
		//			}
		//		System.out.println("is player: " + player);
		//		tar.world.loadedEntityList.add(entity);

		// TeleportationHelper.teleportToPos(entity, tar.getSelfLanding());

		// if (tar.getUpgrades().contains(Upgrade.MOTION)) {
		// entity.motionX = before.xCoord;
		// entity.motionY = before.yCoord;
		// entity.motionZ = before.zCoord;
		// } else {
		// entity.motionX = 0;
		// entity.motionY = 0;
		// entity.motionZ = 0;
		// }
		// if (entity instanceof EntityPlayerMP)
		// ((EntityPlayerMP) entity).connection.netManager.sendPacket(new
		// SPacketEntityVelocity(entity));
		// if (entity instanceof EntityPlayerMP)
		// ((EntityPlayerMP) entity).connection.netManager.sendPacket(new
		// SPacketEntityTeleport(entity));

	}

	@Override
	public List<ItemStack> getDroppingItems() {
		return Lists.newArrayList(stacks);
	}

	@Override
	public boolean openGUI(EntityPlayerMP player) {
		player.openGui(Portals.instance, GuiHandler.PORTAL, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt) {
		switch (NBTHelper.getInt(nbt, "kind")) {
		case BUTTON:
			TileController target = PortalData.get(world).getTile(NBTHelper.getString(nbt, "target"));
			if (target != null)
				setTarget(new GlobalBlockPos(target.getPos(), target.getWorld()));
			break;
		case NAME:
			PortalData data = PortalData.get(world);
			String neu = NBTHelper.getString(nbt, "name");
			if (neu.isEmpty())
				neu = RandomStringUtils.random(10, true, true);
			int i = 1;
			while (data.nameOccupied(neu, new GlobalBlockPos(pos, world))) {
				neu = "Occupied" + i;
				i++;
			}
			setName(neu);
			break;
		case UPGRADE:
			switch (Upgrade.values()[NBTHelper.getInt(nbt, "id")]) {
			case COLOR:
				setColorPortal(NBTHelper.getInt(nbt, "colorP"));
				setColorFrame(NBTHelper.getInt(nbt, "colorF"));
				break;
			case DIRECTION:
				setLooking(getLooking().rotateAround(Axis.Y));
				break;
			case PARTICLE:
				setColorParticle(NBTHelper.getInt(nbt, "color"));
				break;
			}
			break;
		default:
			break;
		}
		sync();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		en.readFromNBT(compound);
		frames = Sets.newHashSet(Utils.getBlockPosList(NBTHelper.getLongList(compound, "frames")));
		portals = Sets.newHashSet(Utils.getBlockPosList(NBTHelper.getLongList(compound, "portals")));
		active = compound.getBoolean("active");
		privat = compound.getBoolean("privat");
		valid = compound.getBoolean("valid");
		owner = NBTHelper.getString(compound, "owner");
		name = NBTHelper.getString(compound, "name");
		targetName = NBTHelper.getString(compound, "targetName");
		target = GlobalBlockPos.loadGlobalPosFromNBT(compound);
		if (compound.hasKey("selfLanding"))
			selfLanding = BlockPos.fromLong(compound.getLong("selfLanding"));
		else
			selfLanding = null;
		if (compound.hasKey("looking"))
			looking = EnumFacing.byName(compound.getString("looking"));
		else
			looking = null;
		this.stacks = new ItemStack[stacks.length];
		for (int i = 0; i < stacks.length; i++)
			stacks[i] = NBTHelper.getItemStackList(compound, "Items").get(i);
		colorParticle = compound.getInteger("colorParticle");
		colorPortal = compound.getInteger("colorPortal");
		colorFrame = compound.getInteger("colorFrame");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		en.writeToNBT(compound);
		NBTHelper.setLongList(compound, "frames", Utils.getLongList(Lists.newArrayList(frames)));
		NBTHelper.setLongList(compound, "frames", Utils.getLongList(Lists.newArrayList(frames)));
		compound.setBoolean("active", active);
		compound.setBoolean("privat", privat);
		compound.setBoolean("valid", valid);
		NBTHelper.setString(compound, "owner", owner);
		NBTHelper.setString(compound, "name", name);
		NBTHelper.setString(compound, "targetName", targetName);
		if (target != null)
			target.writeToNBT(compound);
		if (selfLanding != null)
			compound.setLong("selfLanding", selfLanding.toLong());
		if (looking != null)
			compound.setString("looking", looking.getName2());
		NBTHelper.setItemStackList(compound, "Items", Lists.newArrayList(stacks));
		compound.setInteger("colorParticle", colorParticle);
		compound.setInteger("colorPortal", colorPortal);
		compound.setInteger("colorFrame", colorFrame);

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
		if (target != null)
			setTargetName(((TileController) getTarget().getTile()).getName());
		else {
			if (active)
				deactivate();
		}
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

	public int getColorFrame() {
		return colorFrame;
	}

	public void setColorFrame(int colorFrame) {
		this.colorFrame = colorFrame;
	}

	public EnumFacing getLooking() {
		return looking;
	}

	public void setLooking(EnumFacing looking) {
		this.looking = looking;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	@SuppressWarnings("serial")
	private static class PortalException extends RuntimeException {
		public PortalException(String msg) {
			super(msg);
		}
	}

	@Override
	public TileController getTileController() {
		return this;
	}

	public static boolean portableEntity(Entity entity) {
		return entity instanceof EntityLivingBase || entity instanceof EntityItem;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return en.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return en.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return en.receiveEnergy(maxReceive, simulate);
	}
}
