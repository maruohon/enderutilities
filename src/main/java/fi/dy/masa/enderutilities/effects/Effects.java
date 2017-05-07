package fi.dy.masa.enderutilities.effects;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound.AttenuationType;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;

public class Effects
{
    public static void playSoundEffectServer(World world, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume)
    {
        playSoundEffectServer(world, x, y, z, soundIn, category, volume, 1.0f + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5f);
    }

    public static void playSoundEffectServer(World world, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch)
    {
        world.playSound(null, x, y, z, soundIn, category, volume, pitch);
    }

    public static void playSoundClient(World world, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch)
    {
        world.playSound(x, y, z, soundIn, category, volume, pitch, false);
    }

    @SideOnly(Side.CLIENT)
    public static void playPositionedSoundOnClient(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z)
    {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        SoundEvent sound = SoundEvent.REGISTRY.getObjectById(soundId);

        if (sound != null)
        {
            if (stop)
            {
                soundHandler.stop(sound.getRegistryName().toString(), null);
            }
            else
            {
                PositionedSoundRecord positionedSound = new PositionedSoundRecord(sound.getSoundName(),
                        SoundCategory.RECORDS, volume, pitch, repeat, 0, AttenuationType.LINEAR, x, y, z);
                soundHandler.playSound(positionedSound);
            }
        }
    }

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

    public static void spawnParticlesAround(World world, EnumParticleTypes type, BlockPos pos, int count, Random rand)
    {
        for (int i = 0; i < count; ++i)
        {
            int i1 = rand.nextInt(2) * 2 - 1;
            int j1 = rand.nextInt(2) * 2 - 1;

            double x1 = (double)pos.getX() + 0.5D + 0.25D * (double)i1;
            double y1 = (double)((float)pos.getY() + rand.nextFloat());
            double z1 = (double)pos.getZ() + 0.5D + 0.25D * (double)j1;

            double vx = (double)(rand.nextFloat() * 1.0F * (float)i1);
            double vy = ((double)rand.nextFloat() - 0.5D) * 0.125D;
            double vz = (double)(rand.nextFloat() * 1.0F * (float)j1);

            world.spawnParticle(type, x1, y1, z1, vx, vy, vz);
        }
    }

    public static void spawnParticlesFromServer(int dimension, BlockPos pos, EnumParticleTypes particle)
    {
        spawnParticlesFromServer(dimension, pos, particle, 20);
    }

    public static void spawnParticlesFromServer(int dimension, BlockPos pos, EnumParticleTypes particle, int count)
    {
        spawnParticlesFromServer(dimension, pos, particle, count, 1.8f, 0.9f);
    }

    public static void spawnParticlesFromServer(int dimension, BlockPos pos, EnumParticleTypes particle, int count, float offset, float velocity)
    {
        PacketHandler.INSTANCE.sendToAllAround(new MessageAddEffects(MessageAddEffects.EFFECT_PARTICLES, particle.getParticleID(),
                pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, count, offset, velocity),
                    new NetworkRegistry.TargetPoint(dimension, pos.getX(), pos.getY(), pos.getZ(), 24.0d));
    }

    public static void addItemTeleportEffects(World world, BlockPos pos)
    {
        PacketHandler.INSTANCE.sendToAllAround(
                new MessageAddEffects(MessageAddEffects.EFFECT_ENDER_TOOLS, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND,
                    pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 8, 0.2f, 0.3f),
                    new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 24.0d));
    }
}
