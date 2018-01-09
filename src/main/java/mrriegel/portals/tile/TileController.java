package mrriegel.portals.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.impl.EnergyStorage;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.TeleportationHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.tile.IHUDProvider;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.LimeCapabilities;
import mrriegel.portals.Portals;
import mrriegel.portals.blocks.BlockPortaal;
import mrriegel.portals.gui.GuiHandler;
import mrriegel.portals.init.ModBlocks;
import mrriegel.portals.items.ItemUpgrade.Upgrade;
import mrriegel.portals.util.PortalWorldData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;

public class TileController extends CommonTile implements IPortalFrame, IEnergyReceiver {

	public static final int untilPort = 9;
	public static final int BUTTON = 0, NAME = 1, UPGRADE = 2;
	private static final int MAXFRAMES = 300;

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private List<ItemStack> stacks = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY);
	private boolean privat, active, valid;
	private String owner, name = WordUtils.capitalize(RandomStringUtils.random(10, true, !true).toLowerCase()), targetName;
	private GlobalBlockPos target;
	private BlockPos selfLanding;
	private int colorPortal = 0x9500fe, colorParticle = 0x9500fe, colorFrame = 0x9500fe;
	private EnumFacing looking = EnumFacing.NORTH;
	private Axis axis = null;

	public EnergyStorage en = new EnergyStorage(64000, 1000, 0);

	public boolean validatePortal() {
		Set<EnumFacing> faces = Sets.newHashSet();
		Set<BlockPos> set = getPortalBlocks(faces);
		if (portals == null || !portals.equals(set)) {
			valid = !set.isEmpty();
			if (!valid && active)
				deactivate();
			portals = set;
			// System.out.println("fac: "+faces.size());
			if (!faces.isEmpty())
				addFrames(faces);
			Set<Integer> x = Sets.newHashSet(), y = Sets.newHashSet(), z = Sets.newHashSet();
			for (BlockPos p : frames) {
				x.add(p.getX());
				y.add(p.getY());
				z.add(p.getZ());
			}
			if (x.size() == 1)
				axis = Axis.Z;
			else if (y.size() == 1)
				axis = Axis.Y;
			else if (z.size() == 1)
				axis = Axis.X;
			Validate.notNull(axis, "What??");
		}
		if (valid) {
			PortalWorldData.getData(world).add(pos);
			calculateLanding();
		} else {
			PortalWorldData.getData(world).remove(pos);
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
		for (BlockPos p : portals) {
			world.setBlockState(p, ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS, axis));
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
		// if (worldObj.getTileEntity(p) instanceof TileFrame) {
		// ((TileFrame) worldObj.getTileEntity(p)).setController(null);
		// ((TileFrame) worldObj.getTileEntity(p)).sync();
		// }
		active = false;
		sync();
		for (BlockPos p : frames)
			if (world.getTileEntity(p) instanceof TileFrame)
				((TileFrame) world.getTileEntity(p)).sync();

	}

	private void calculateLanding() {
		List<BlockPos> selfs = new ArrayList<>();
		for (BlockPos p : portals) {
			IBlockState a = world.getBlockState(p);
			IBlockState aPlus = world.getBlockState(p.up());
			if (a.getCollisionBoundingBox(world, p) == null && aPlus.getCollisionBoundingBox(world, p.up()) == null && !portals.contains(p.down())) {
				selfs.add(p);
			}
		}
		if (selfs.isEmpty())
			selfLanding = null;
		else {
			if (axis == Axis.X || axis == Axis.Z) {
				selfs.sort((a, b) -> {
					int x = Integer.compare(a.getY(), b.getY());
					return x;
				});
				Integer low = null;
				Iterator<BlockPos> it = selfs.iterator();
				while (it.hasNext()) {
					BlockPos p = it.next();
					if (low == null)
						low = p.getY();
					else {
						if (p.getY() > low)
							it.remove();
					}
				}
			}
			System.out.println(axis);
			selfs.sort((a, b) -> {
				int x = Integer.compare(a.getX() + a.getZ(), b.getX() + b.getZ());
				return x;
			});

			selfLanding = selfs.get(MathHelper.clamp(selfs.size() / 2, 0, selfs.size() - 1));
		}
	}

	public Set<BlockPos> getPortalBlocks(Set<EnumFacing> faces) {
		Set<BlockPos> ps = Sets.newHashSet();
		Set<Pair<Set<BlockPos>, Pair<EnumFacing, Integer>>> sets = Sets.newHashSet();
		for (EnumFacing f : EnumFacing.VALUES) {
			for (int i = 0; i < 2; i++) {
				Set<BlockPos> portals = Sets.newHashSet();
				if (!validPortalPos(pos.offset(f)))
					continue;
				portals.add(pos.offset(f));
				try {
					addPortals(pos.offset(f), portals, valids(f, i));
					if (portals.size() <= MAXFRAMES && validNeighbors(portals, valids(f, i)))
						sets.add(Pair.of(portals, Pair.of(f, i)));
				} catch (PortalException e) {
					// Portals.logger.error(e.getLocalizedMessage());
				}
			}
		}

		Pair<Set<BlockPos>, Pair<EnumFacing, Integer>> fin = null;
		for (Pair<Set<BlockPos>, Pair<EnumFacing, Integer>> pa : sets) {
			if (fin == null || pa.getLeft().size() > fin.getLeft().size())
				fin = Pair.of(pa.getLeft(), pa.getRight());
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
		if (portals.size() > MAXFRAMES)
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
			// if (!worldObj.isAirBlock(bl) && !validFrame(bl)) {
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
		entity.getEntityData().setBoolean("ported", true);
		Vec3d motion = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
		System.out.println(entity.motionX + " " + entity.motionY + " " + entity.motionZ + " ");
		TeleportationHelper.teleport(entity, tar.getSelfLanding(), target.getDimension());
		if (tar.getUpgrades().contains(Upgrade.DIRECTION) && tar.looking != null) {
			if (entity instanceof EntityPlayerMP) {
				EntityPlayerMP player = (EntityPlayerMP) entity;
				player.rotationYaw = tar.looking.getHorizontalAngle();
				player.connection.sendPacket(new SPacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, Sets.<SPacketPlayerPosLook.EnumFlags> newHashSet(), 1000));
			}
		}

		if (tar.getUpgrades().contains(Upgrade.MOTION))
			if (entity instanceof EntityPlayerMP)
				((EntityPlayerMP) entity).connection.netManager.sendPacket(new SPacketEntityVelocity(entity.getEntityId(), motion.x, motion.y, motion.z));
			else {
				entity.motionX = motion.x;
				entity.motionY = motion.y;
				entity.motionZ = motion.z;
			}
	}

	@Override
	public List<ItemStack> getDroppingItems() {
		return Lists.newArrayList(stacks);
	}

	@Override
	public boolean openGUI(EntityPlayerMP player) {
		if ("!".isEmpty()) {
			validatePortal();
			if (selfLanding != null)
				TeleportationHelper.teleport(player, selfLanding);
			return false;
		}
		if (!valid) {
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Invalid Structure"), true);
			return true;
		}
		player.openGui(Portals.instance, GuiHandler.PORTAL, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == LimeCapabilities.hudproviderCapa || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == LimeCapabilities.hudproviderCapa) {
			return (T) new IHUDProvider() {

				@Override
				public List<String> getData(boolean sneak, EnumFacing facing) {
					List<String> lis = new ArrayList<>();
					lis.add(TextFormatting.YELLOW + name);
					lis.add((valid ? TextFormatting.GREEN + "Valid " : TextFormatting.RED + "Invalid ") + "Structure");
					lis.add(active ? TextFormatting.GREEN + "On" : TextFormatting.RED + "Off");
					if (targetName != null)
						lis.add("Target: " + targetName);

					return lis;
				}
			};
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt) {
		switch (NBTHelper.get(nbt, "kind", int.class)) {
		case BUTTON:
			TileController target = PortalWorldData.getData(world).getTile(NBTHelper.get(nbt, "target", String.class));
			if (target != null)
				setTarget(GlobalBlockPos.fromTile(target));
			break;
		case NAME:
			PortalWorldData data = PortalWorldData.getData(world);
			String neu = NBTHelper.get(nbt, "name", String.class);
			if (neu.isEmpty())
				neu = RandomStringUtils.random(10, true, true);
			int i = 1;
			while (data.nameOccupied(neu, pos)) {
				neu = "Occupied" + i;
				i++;
			}
			setName(neu);
			break;
		case UPGRADE:
			switch (Upgrade.values()[NBTHelper.get(nbt, "id", int.class)]) {
			case COLOR:
				setColorPortal(NBTHelper.get(nbt, "colorP", int.class));
				setColorFrame(NBTHelper.get(nbt, "colorF", int.class));
				break;
			case DIRECTION:
				setLooking(getLooking().rotateAround(Axis.Y));
				break;
			case PARTICLE:
				setColorParticle(NBTHelper.get(nbt, "color", int.class));
				break;
			}
			break;
		default:
			break;
		}
		markForSync();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		en.readFromNBT(nbt);
		frames = new HashSet<>(NBTHelper.getList(nbt, "frames", BlockPos.class));
		portals = new HashSet<>(NBTHelper.getList(nbt, "portals", BlockPos.class));
		stacks = NBTHelper.getList(nbt, "stacks", ItemStack.class);
		privat = NBTHelper.get(nbt, "privat", boolean.class);
		active = NBTHelper.get(nbt, "active", boolean.class);
		valid = NBTHelper.get(nbt, "valid", boolean.class);
		owner = NBTHelper.get(nbt, "owner", String.class);
		name = NBTHelper.get(nbt, "name", String.class);
		targetName = NBTHelper.get(nbt, "targetName", String.class);
		target = GlobalBlockPos.loadGlobalPosFromNBT(NBTHelper.getSafe(nbt, "target", NBTTagCompound.class).orElse(new NBTTagCompound()));
		selfLanding = NBTHelper.get(nbt, "self", BlockPos.class);
		colorPortal = NBTHelper.get(nbt, "colorPortal", int.class);
		colorParticle = NBTHelper.get(nbt, "colorParticle", int.class);
		colorFrame = NBTHelper.get(nbt, "colorFrame", int.class);
		looking = NBTHelper.get(nbt, "looking", EnumFacing.class);
		axis = NBTHelper.get(nbt, "axis", Axis.class);

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		en.writeToNBT(nbt);
		NBTHelper.setList(nbt, "frames", new ArrayList<>(frames));
		NBTHelper.setList(nbt, "portals", new ArrayList<>(portals));
		NBTHelper.setList(nbt, "stacks", stacks);
		NBTHelper.set(nbt, "privat", privat);
		NBTHelper.set(nbt, "active", active);
		NBTHelper.set(nbt, "valid", valid);
		NBTHelper.set(nbt, "owner", owner);
		NBTHelper.set(nbt, "name", name);
		NBTHelper.set(nbt, "targetName", targetName);
		NBTHelper.set(nbt, "target", target == null ? null : target.writeToNBT(new NBTTagCompound()));
		NBTHelper.set(nbt, "self", selfLanding);
		NBTHelper.set(nbt, "colorPortal", colorPortal);
		NBTHelper.set(nbt, "colorParticle", colorParticle);
		NBTHelper.set(nbt, "colorFrame", colorFrame);
		NBTHelper.set(nbt, "looking", looking);
		NBTHelper.set(nbt, "axis", axis);

		return super.writeToNBT(nbt);
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
			setTargetName(null);
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

	public List<ItemStack> getStacks() {
		return stacks;
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

	private static class PortalException extends RuntimeException {
		public PortalException(String msg) {
			super(msg);
		}
	}

	public static boolean isPortalActive(GlobalBlockPos pos) {
		if (pos == null)
			return false;
		boolean valid = PortalWorldData.getData(pos.getWorld()).validPos(pos.getPos());
		if (!valid)
			return false;
		if (pos.getTile() instanceof TileController && ((TileController) pos.getTile()).isActive() // &&
		// ((TileController)
		// pos.getTile()).getTarget().equals(new
		// GlobalBlockPos(this.pos,
		// worldObj))
		)
			return true;
		// for (EnumFacing face : EnumFacing.VALUES) {
		// if
		// (pos.getWorld(worldObj).getBlockState(pos.getPos().offset(face)).getBlock()
		// == ModBlocks.portaal)
		// return true;
		// }
		return false;
	}
}
