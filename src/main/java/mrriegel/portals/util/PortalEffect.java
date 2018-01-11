package mrriegel.portals.util;

import java.awt.Color;

import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.world.World;

public class PortalEffect extends ParticlePortal {

	public PortalEffect(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int color) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		float f = this.rand.nextFloat() * 0.6F + 0.4F;
		Color c = new Color(color);
		this.particleRed = f * c.getRed() / 255f;
		this.particleGreen = f * c.getGreen() / 255f;
		this.particleBlue = f * c.getBlue() / 255f;
	}

}
