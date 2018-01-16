package mrriegel.classicportals.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Sets;

import cofh.redstoneflux.api.IEnergyReceiver;
import mrriegel.classicportals.ClassicPortals;
import mrriegel.classicportals.ModConfig;
import mrriegel.classicportals.blocks.BlockPortaal;
import mrriegel.classicportals.gui.GuiHandler;
import mrriegel.classicportals.init.ModBlocks;
import mrriegel.classicportals.init.ModItems;
import mrriegel.classicportals.items.Upgrade;
import mrriegel.classicportals.util.PortalWorldData;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.helper.TeleportationHelper;
import mrriegel.limelib.tile.CommonTile;
import mrriegel.limelib.tile.IHUDProvider;
import mrriegel.limelib.util.EnergyStorageExt;
import mrriegel.limelib.util.GlobalBlockPos;
import mrriegel.limelib.util.LimeCapabilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileController extends CommonTile implements IEnergyReceiver {

	public static final int untilPort = 9;
	public static final int BUTTON = 0, NAME = 1, UPGRADE = 2, ACTIVATE = 3;
	private static final int MAXFRAMES = 800;

	public static enum RedstoneMode {
		ON, OFF, TOGGLE;
	}

	private Set<BlockPos> frames = Sets.newHashSet(), portals = Sets.newHashSet();
	private List<ItemStack> stacks = NonNullList.<ItemStack> withSize(4, ItemStack.EMPTY);
	private boolean privat, active, valid, powered;
	private String owner, name = WordUtils.capitalize(RandomStringUtils.random(10, true, !true).toLowerCase()), targetName;
	private GlobalBlockPos target;
	private BlockPos selfLanding;
	private int colorPortal = 0xADD8E6, colorParticle = 0xFFFFFF, colorFrame = 0xFFFFFF;
	private EnumFacing looking = EnumFacing.NORTH;
	private RedstoneMode red = RedstoneMode.ON;
	private Axis axis = null;

	public EnergyStorageExt en = new EnergyStorageExt(500000, 1000, 0);

	public boolean validatePortal() {
		Set<EnumFacing> faces = Sets.newHashSet();
		Set<BlockPos> set = getPortalBlocks(faces);
		if (portals == null || !portals.equals(set)) {
			valid = !set.isEmpty();
			Set<BlockPos> prevPortals = new HashSet<>(portals);
			portals = set;
			if (!faces.isEmpty())
				addFrames(faces);
			if (valid)
				valid = frames.stream().filter(p -> world.getTileEntity(p) instanceof TileCapa).count() <= 1;
			if (valid)
				valid = frames.stream().allMatch(p -> world.getTileEntity(p) instanceof TileBasicFrame);
			if (active && (!valid || (valid && portals.size() != prevPortals.size()))) {
				portals = prevPortals;
				deactivate();
				portals = set;
			}
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
		}
		if (valid) {
			PortalWorldData.INSTANCE.add(new GlobalBlockPos(pos, world));
			calculateLanding();
		} else {
			PortalWorldData.INSTANCE.remove(new GlobalBlockPos(pos, world));
			selfLanding = null;
		}
		markForSync();
		return valid;
	}

	public boolean activate() {
		if (!validatePortal()) {
			return false;
		}
		if (target == null) {
			return false;
		}
		TileController tc = (TileController) target.getTile();
		if (!tc.validatePortal())
			return false;
		for (BlockPos p : portals) {
			world.setBlockState(p, ModBlocks.portaal.getDefaultState().withProperty(BlockPortaal.AXIS, axis), 2);
			((TilePortaal) world.getTileEntity(p)).setController(this.pos);
			((TilePortaal) world.getTileEntity(p)).markForSync();
		}
		active = true;
		markForSync();
		for (BlockPos p : frames)
			if (world.getTileEntity(p) instanceof TileBasicFrame)
				((TileBasicFrame) world.getTileEntity(p)).markForSync();
		if (GlobalBlockPos.fromTile(this).equals(tc.target)) {
			if (!tc.active)
				return tc.activate();
		} else {
			if (tc.target != null)
				tc.deactivate();
			tc.setTarget(GlobalBlockPos.fromTile(this));
			return tc.activate();
		}
		return true;

	}

	public void deactivate() {
		for (BlockPos p : portals)
			if (world.getBlockState(p).getBlock() == ModBlocks.portaal) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
			}
		if (active) {
			active = false;
			if (target != null) {
				TileController tc = (TileController) target.getTile();
				tc.deactivate();
			}
		}
		markForSync();

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
			Map<BlockPos, Double> map = new HashMap<>();
			for (BlockPos p : selfs) {
				double dis = 0;
				for (BlockPos pp : selfs) {
					dis += pp.distanceSq(p);
				}
				map.put(p, dis);
			}

			selfLanding = map.entrySet().stream().reduce((e1, e2) -> e1.getValue() < e2.getValue() ? e1 : e2).get().getKey();
		}
	}

	public Set<BlockPos> getPortalBlocks(Set<EnumFacing> faces) {
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
				}
			}
		}

		Pair<Set<BlockPos>, Pair<EnumFacing, Integer>> fin = null;
		for (Pair<Set<BlockPos>, Pair<EnumFacing, Integer>> pa : sets) {
			if (fin == null || pa.getLeft().size() > fin.getLeft().size())
				fin = Pair.of(pa.getLeft(), pa.getRight());
		}
		Set<BlockPos> ps = fin != null && fin.getLeft() != null ? Sets.newHashSet(fin.getLeft()) : Sets.newHashSet();
		if (fin != null && fin.getRight() != null)
			faces.addAll(valids(fin.getRight().getLeft(), fin.getRight().getRight()));
		return ps;
	}

	private void addPortals(BlockPos pos, Set<BlockPos> portals, Set<EnumFacing> faces) {
		if (portals.size() >= MAXFRAMES)
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
		}
	}

	private boolean validPortalPos(BlockPos p) {
		return world.isAirBlock(p) || world.getBlockState(p).getBlock() == ModBlocks.portaal;
	}

	private boolean validFrame(BlockPos p) {
		return (world.getTileEntity(p) instanceof TileBasicFrame && (((TileBasicFrame) world.getTileEntity(p)).getController() == null || ((TileBasicFrame) world.getTileEntity(p)).getController().equals(pos))) || p.equals(this.pos);
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

	public void addFrames(Set<EnumFacing> faces) {
		frames.clear();
		for (BlockPos p : portals) {
			for (EnumFacing face : faces) {
				BlockPos pp = p.offset(face);
				if (world.getTileEntity(pp) instanceof TileBasicFrame) {
					frames.add(pp);
					((TileBasicFrame) world.getTileEntity(pp)).setController(this.pos);
					((TileBasicFrame) world.getTileEntity(pp)).markForSync();
				}
			}
		}
	}

	public Set<Upgrade> getUpgrades() {
		Set<Upgrade> set = Sets.newHashSet();
		for (ItemStack stack : stacks) {
			if (stack.getItem() == ModItems.upgrade) {
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
		//		Vec3d motion = new Vec3d(entity.motionX, entity.motionY, entity.motionZ);
		int need = !ModConfig.energyNeeded ? 0 : target.getDimension() != world.provider.getDimension() ? 100000 : //
				(int) Math.sqrt(tar.getPos().distanceSq(pos)) * 20;
		need = Math.min(need, en.getMaxEnergyStored());
		if (need > en.getEnergyStored())
			return;
		entity.getEntityData().setBoolean("ported", true);
		Optional<Entity> op = TeleportationHelper.teleport(entity, tar.getSelfLanding(), target.getDimension());
		if (op.isPresent()) {
			en.modifyEnergyStored(-need);
			entity = op.get();
			entity.fallDistance = 0f;
			//			world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, .8f, .8f);
			if (tar.getUpgrades().contains(Upgrade.DIRECTION) && tar.looking != null) {
				if (entity instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) entity;
					player.rotationYaw = tar.looking.getHorizontalAngle();
					player.connection.sendPacket(new SPacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, Sets.<SPacketPlayerPosLook.EnumFlags> newHashSet(), 1000));
				} else
					entity.rotationYaw = tar.looking.getHorizontalAngle();
			} else {
				EnumFacing f = entity.getHorizontalFacing();
				BlockPos ent = new BlockPos(entity);
				BlockPos front = ent.offset(f);
				if (entity.world.getBlockState(front).getCollisionBoundingBox(entity.world, front) != null) {
					EnumFacing fin = null;
					for (int i = 0; i < 4; i++) {
						f = f.rotateY();
						if (entity.world.getBlockState(ent.offset(f)).getCollisionBoundingBox(entity.world, ent.offset(f)) == null) {
							fin = f;
							break;
						}
					}
					if (fin != null) {
						if (entity instanceof EntityPlayerMP) {
							EntityPlayerMP player = (EntityPlayerMP) entity;
							player.rotationYaw = fin.getHorizontalAngle();
							player.connection.sendPacket(new SPacketPlayerPosLook(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch, Sets.<SPacketPlayerPosLook.EnumFlags> newHashSet(), 1000));
						} else
							entity.rotationYaw = fin.getHorizontalAngle();
					}
				}
			}
			//			if (tar.getUpgrades().contains(Upgrade.MOTION))
			//				if (entity instanceof EntityPlayerMP)
			//					((EntityPlayerMP) entity).connection.netManager.sendPacket(new SPacketEntityVelocity(entity.getEntityId(), motion.x, motion.y, motion.z));
			//				else {
			//					entity.motionX = motion.x;
			//					entity.motionY = motion.y;
			//					entity.motionZ = motion.z;
			//				}
		}
	}

	public void repaintPortal() {
		PortalWorldData.INSTANCE.refreshColors();
		for (BlockPos p : getFrames())
			world.markBlockRangeForRenderUpdate(p.add(-1, -1, -1), p.add(1, 1, 1));
		for (BlockPos p : getPortals())
			world.markBlockRangeForRenderUpdate(p.add(-1, -1, -1), p.add(1, 1, 1));
	}

	@Override
	public List<ItemStack> getDroppingItems() {
		return stacks;
	}

	@Override
	public boolean openGUI(EntityPlayerMP player) {
		validatePortal();
		if (!valid) {
			player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Invalid Structure"), true);
			return true;
		}
		player.openGui(ClassicPortals.instance, GuiHandler.PORTAL, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return (capability == CapabilityEnergy.ENERGY && ModConfig.energyNeeded) || capability == LimeCapabilities.hudproviderCapa || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == LimeCapabilities.hudproviderCapa) {
			return (T) new IHUDProvider() {

				@Override
				public List<String> getData(boolean sneak, EnumFacing facing) {
					List<String> lis = new ArrayList<>();
					lis.add(TextFormatting.UNDERLINE + "" + TextFormatting.YELLOW + name);
					lis.add((valid ? TextFormatting.GREEN + "Valid " : TextFormatting.DARK_RED + "Invalid ") + "Structure");
					lis.add(active ? TextFormatting.GREEN + "On" : TextFormatting.DARK_RED + "Off");
					if (targetName != null)
						lis.add("Target: " + targetName);

					return lis;
				}

				@Override
				public boolean lineBreak(boolean sneak, EnumFacing facing) {
					return false;
				}

				@Override
				public int getBackgroundColor(boolean sneak, EnumFacing facing) {
					return 0x888E8E8E;
				}

				@Override
				public double scale(boolean sneak, EnumFacing facing) {
					return 1.2;
				}

			};
		}
		if (capability == CapabilityEnergy.ENERGY && ModConfig.energyNeeded) {
			return (T) en;
		}
		return super.getCapability(capability, facing);
	}

	public void setColors(boolean reset) {
		if (!reset)
			PortalWorldData.INSTANCE.refreshColors();
		else {
			for (BlockPos p : getFrames()) {
				PortalWorldData.INSTANCE.frameColors.remove(new GlobalBlockPos(p, world));
			}
			for (BlockPos p : getPortals()) {
				PortalWorldData.INSTANCE.portalColors.remove(new GlobalBlockPos(p, world));
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		setColors(true);
		PortalWorldData.INSTANCE.remove(new GlobalBlockPos(pos, world));
		for (BlockPos p : getFrames()) {
			if (world.getTileEntity(p) instanceof TileBasicFrame) {
				((TileBasicFrame) world.getTileEntity(p)).setController(null);
			}
		}
		for (BlockPos p : getPortals()) {
			if (world.getTileEntity(p) instanceof TilePortaal) {
				((TilePortaal) world.getTileEntity(p)).setController(null);
			}
		}
		deactivate();
	}

	@Override
	public void neighborChanged(IBlockState state, Block block, BlockPos fromPos) {
		super.neighborChanged(state, block, fromPos);
		validatePortal();
		setPowered(world.isBlockPowered(pos));
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt) {
		switch (NBTHelper.get(nbt, "kind", int.class)) {
		case BUTTON:
			if (world.isRemote)
				break;
			String name = NBTHelper.get(nbt, "target", String.class);
			TileController target = PortalWorldData.INSTANCE.getTile(name);
			if (name.equals(targetName))
				setTarget(null);
			else if (target != null) {
				if (this.target != null)
					deactivate();
				setTarget(GlobalBlockPos.fromTile(target));
			}
			break;
		case NAME:
			if (world.isRemote)
				break;
			String neu = NBTHelper.get(nbt, "name", String.class);
			if (neu.equals(this.name))
				break;
			if (neu.isEmpty())
				neu = RandomStringUtils.random(10, true, !true);
			else {
				int i = 1;
				while (PortalWorldData.INSTANCE.nameOccupied(neu, new GlobalBlockPos(pos, world))) {
					neu = "Occupied" + i;
					i++;
				}
				setName(neu);
			}
			PortalWorldData.INSTANCE.sync(null);
			break;
		case UPGRADE:
			switch (Upgrade.values()[NBTHelper.get(nbt, "id", int.class)]) {
			case COLOR:
				setColorPortal(NBTHelper.get(nbt, "colorP", int.class));
				setColorFrame(NBTHelper.get(nbt, "colorF", int.class));
				setColors(false);
				break;
			case DIRECTION:
				setLooking(getLooking().rotateAround(Axis.Y));
				break;
			case PARTICLE:
				setColorParticle(NBTHelper.get(nbt, "color", int.class));
				break;
			case REDSTONE:
				setRed(RedstoneMode.values()[(getRed().ordinal() + 1) % RedstoneMode.values().length]);
				break;
			}
			break;
		case ACTIVATE:
			if (active)
				deactivate();
			else
				activate();
			break;
		default:
			break;
		}
		markForSync();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		en.setEnergyStored(NBTHelper.get(nbt, "energy", int.class));
		frames = new HashSet<>(NBTHelper.getList(nbt, "frames", BlockPos.class));
		portals = new HashSet<>(NBTHelper.getList(nbt, "portals", BlockPos.class));
		stacks = NBTHelper.getList(nbt, "stacks", ItemStack.class);
		privat = NBTHelper.get(nbt, "privat", boolean.class);
		active = NBTHelper.get(nbt, "active", boolean.class);
		valid = NBTHelper.get(nbt, "valid", boolean.class);
		powered = NBTHelper.get(nbt, "powered", boolean.class);
		owner = NBTHelper.get(nbt, "owner", String.class);
		name = NBTHelper.get(nbt, "name", String.class);
		targetName = NBTHelper.get(nbt, "targetName", String.class);
		target = GlobalBlockPos.loadGlobalPosFromNBT(NBTHelper.getSafe(nbt, "target", NBTTagCompound.class).orElse(new NBTTagCompound()));
		selfLanding = NBTHelper.get(nbt, "self", BlockPos.class);
		colorPortal = NBTHelper.get(nbt, "colorPortal", int.class);
		colorParticle = NBTHelper.get(nbt, "colorParticle", int.class);
		colorFrame = NBTHelper.get(nbt, "colorFrame", int.class);
		looking = NBTHelper.get(nbt, "looking", EnumFacing.class);
		red = NBTHelper.get(nbt, "red", RedstoneMode.class);
		axis = NBTHelper.get(nbt, "axis", Axis.class);

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTHelper.set(nbt, "energy", en.getEnergyStored());
		NBTHelper.setList(nbt, "frames", new ArrayList<>(frames));
		NBTHelper.setList(nbt, "portals", new ArrayList<>(portals));
		NBTHelper.setList(nbt, "stacks", stacks);
		NBTHelper.set(nbt, "privat", privat);
		NBTHelper.set(nbt, "active", active);
		NBTHelper.set(nbt, "valid", valid);
		NBTHelper.set(nbt, "powered", powered);
		NBTHelper.set(nbt, "owner", owner);
		NBTHelper.set(nbt, "name", name);
		NBTHelper.set(nbt, "targetName", targetName);
		NBTHelper.set(nbt, "target", target == null ? null : target.writeToNBT(new NBTTagCompound()));
		NBTHelper.set(nbt, "self", selfLanding);
		NBTHelper.set(nbt, "colorPortal", colorPortal);
		NBTHelper.set(nbt, "colorParticle", colorParticle);
		NBTHelper.set(nbt, "colorFrame", colorFrame);
		NBTHelper.set(nbt, "looking", looking);
		NBTHelper.set(nbt, "red", red);
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

	public boolean isPowered() {
		return powered;
	}

	public void setPowered(boolean powered) {
		if (!world.isRemote && getUpgrades().contains(Upgrade.REDSTONE))
			switch (red) {
			case OFF:
				if (!powered && !active)
					activate();
				else if (powered && active)
					deactivate();
				break;
			case ON:
				if (powered && !active)
					activate();
				else if (!powered && active)
					deactivate();
				break;
			case TOGGLE:
				if (!this.powered && powered)
					if (active)
						deactivate();
					else
						activate();
				break;
			default:
				break;
			}
		this.powered = powered;
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
		PortalWorldData.INSTANCE.sync(null);
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

	public RedstoneMode getRed() {
		return red;
	}

	public void setRed(RedstoneMode red) {
		this.red = red;
	}

	public Axis getAxis() {
		return axis;
	}

	public void setAxis(Axis axis) {
		this.axis = axis;
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
		return ModConfig.energyNeeded;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return en.receiveEnergy(maxReceive, simulate);
	}

	@SuppressWarnings("serial")
	private static class PortalException extends RuntimeException {
		public PortalException(String msg) {
			super(msg);
		}
	}

	public static boolean isPortalActive(GlobalBlockPos pos) {
		if (pos == null)
			return false;
		boolean valid = PortalWorldData.INSTANCE.validPos(pos);
		if (!valid)
			return false;
		return pos.getTile() instanceof TileController && ((TileController) pos.getTile()).isActive();
	}
}
