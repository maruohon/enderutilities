package fi.dy.masa.enderutilities.client.effects;

import java.util.Random;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Particles
{
    public static void spawnParticles(World world, EnumParticleTypes type, double x, double y, double z, int count, double offset, double velocity)
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
            world.spawnParticle(type, x + offX, y + offY, z + offZ, -velX, -velY, -velZ);
        }
    }

    public static void spawnParticlesAround(World world, EnumParticleTypes type, int x, int y, int z, int count, Random rand)
    {
        for (int i = 0; i < count; ++i)
        {
            int i1 = rand.nextInt(2) * 2 - 1;
            int j1 = rand.nextInt(2) * 2 - 1;

            double x1 = (double)x + 0.5D + 0.25D * (double)i1;
            double y1 = (double)((float)y + rand.nextFloat());
            double z1 = (double)z + 0.5D + 0.25D * (double)j1;

            double vx = (double)(rand.nextFloat() * 1.0F * (float)i1);
            double vy = ((double)rand.nextFloat() - 0.5D) * 0.125D;
            double vz = (double)(rand.nextFloat() * 1.0F * (float)j1);

            world.spawnParticle(type, x1, y1, z1, vx, vy, vz);
        }
    }
}
