package fi.dy.masa.enderutilities.client.effects;

import net.minecraft.world.World;

public class Sounds
{
    public static void playSound(World world, double x, double y, double z, String name, float volume)
    {
        world.playSoundEffect(x, y, z, name, volume, 1.0f + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5f);
    }

    public static void playSound(World world, double x, double y, double z, String name, float volume, float pitch)
    {
        world.playSoundEffect(x, y, z, name, volume, pitch);
    }
}
