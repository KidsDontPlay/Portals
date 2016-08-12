package mrriegel.portals;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class PortalTeleporter extends Teleporter {

	BlockPos pos;
	WorldServer world;

	public PortalTeleporter(WorldServer worldIn, BlockPos pos) {
		super(worldIn);
		this.world = worldIn;
		this.pos = pos;
//		System.out.println("one: "+pos);
	}

	@Override
	public void placeInPortal(Entity entityIn, float rotationYaw) {
		// super.placeInPortal(entityIn, rotationYaw);
		this.world.getBlockState(pos);
		entityIn.setPosition(pos.getX() + .5, pos.getY() + .05, pos.getZ() + .5);
//		System.out.println("two: "+pos);
//		System.out.println(entityIn);
	}

}
