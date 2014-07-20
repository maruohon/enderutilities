package fi.dy.masa.enderutilities.client.effects;

import net.minecraft.world.World;

public class Particles
{
	public static void enderParticles(World world, double x, double y, double z, int count, double offset, double velocity)
	{
		// Spawn some particles
		for (int i = 0; i < count; i++)
		{
			double offX = (world.rand.nextFloat() - 0.5d) * offset;
			double offY = (world.rand.nextFloat() - 0.5d) * offset;
			double offZ = (world.rand.nextFloat() - 0.5d) * offset;

			double velX = (world.rand.nextFloat() - 0.5d) * velocity;
			double velY = (world.rand.nextFloat() - 0.5d) * velocity;
			double velZ = (world.rand.nextFloat() - 0.5d) * velocity;
			world.spawnParticle("portal", x + offX, y + offY, z + offZ, -velX, -velY, -velZ);
		}
	}
}
