package fi.dy.masa.enderutilities.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.effects.Effects;
import io.netty.buffer.ByteBuf;

public class MessageAddEffects implements IMessage
{
    public static final int SOUND = 1;
    public static final int PARTICLES = 2;

    public static final int EFFECT_TELEPORT = 1;
    public static final int EFFECT_ENDER_TOOLS = 2;
    public static final int EFFECT_PARTICLES = 100;
    public static final int EFFECT_SOUND_EVENT = 110;

    private int effectType;
    private int flags;
    private float x;
    private float y;
    private float z;
    private int particleCount;
    private double offset;
    private double velocity;

    private int soundEventId;
    private float pitch;
    private float volume;
    private boolean repeat;

    public MessageAddEffects()
    {
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z)
    {
        this(id, flags, x, y, z, 32, 0.2f, 2.0f);
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z, int particleCount)
    {
        this(id, flags, x, y, z, particleCount, 0.2f, 2.0f);
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z, int particleCount, float offset, float velocity)
    {
        this.effectType = id;
        this.flags = flags;
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.particleCount = particleCount;
        this.offset = offset;
        this.velocity = velocity;
    }

    public MessageAddEffects(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z)
    {
        this.effectType = EFFECT_SOUND_EVENT;
        this.soundEventId = soundId;
        this.flags = stop ? 1 : 0;
        this.pitch = pitch;
        this.volume = volume;
        this.repeat = repeat;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.effectType);
        buf.writeByte(this.flags);
        buf.writeFloat((float) this.x);
        buf.writeFloat((float) this.y);
        buf.writeFloat((float) this.z);

        if (this.effectType == EFFECT_SOUND_EVENT)
        {
            buf.writeShort((short) this.soundEventId);
            buf.writeFloat(this.pitch);
            buf.writeFloat(this.volume);
            buf.writeBoolean(this.repeat);
        }
        else
        {
            buf.writeShort(this.particleCount);
            buf.writeFloat((float) this.offset);
            buf.writeFloat((float) this.velocity);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.effectType = buf.readByte();
        this.flags = buf.readByte();
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();

        if (this.effectType == EFFECT_SOUND_EVENT)
        {
            this.soundEventId = buf.readShort();
            this.pitch = buf.readFloat();
            this.volume = buf.readFloat();
            this.repeat = buf.readBoolean();
        }
        else
        {
            this.particleCount = buf.readShort();
            this.offset = buf.readFloat();
            this.velocity = buf.readFloat();
        }
    }

    public static class Handler implements IMessageHandler<MessageAddEffects, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageAddEffects message, MessageContext ctx)
        {
            if (ctx.side != Side.CLIENT)
            {
                EnderUtilities.logger.error("Wrong side in MessageAddEffects: " + ctx.side);
                return null;
            }

            Minecraft mc = FMLClientHandler.instance().getClient();
            final EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
            if (mc == null || player == null)
            {
                EnderUtilities.logger.error("Minecraft or player was null in MessageAddEffects");
                return null;
            }

            mc.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message, player, player.getEntityWorld(), mc.getSoundHandler());
                }
            });

            return null;
        }

        protected void processMessage(final MessageAddEffects message, EntityPlayer player, World world, SoundHandler soundHandler)
        {
            if (message.effectType == EFFECT_TELEPORT)
            {
                if ((message.flags & SOUND) == SOUND)
                {
                    float pitch = 0.9f + world.rand.nextFloat() * 0.125f + world.rand.nextFloat() * 0.125f;
                    Effects.playSoundClient(world, message.x, message.y, message.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.8f, pitch);
                }
                if ((message.flags & PARTICLES) == PARTICLES)
                {
                    Effects.spawnParticles(world, EnumParticleTypes.PORTAL, message.x, message.y, message.z, message.particleCount, message.offset, message.velocity);
                }
            }
            else if (message.effectType == EFFECT_ENDER_TOOLS)
            {
                if ((message.flags & SOUND) == SOUND && Configs.useToolSounds)
                {
                    Effects.playSoundClient(world, message.x, message.y, message.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.HOSTILE, 0.08f, 1.8f);
                }
                if ((message.flags & PARTICLES) == PARTICLES && Configs.useToolParticles)
                {
                    Effects.spawnParticles(world, EnumParticleTypes.PORTAL, message.x, message.y, message.z, message.particleCount, message.offset, message.velocity);
                }
            }
            else if (message.effectType == EFFECT_PARTICLES)
            {
                Effects.spawnParticles(world, EnumParticleTypes.getParticleFromId(message.flags), message.x, message.y, message.z, message.particleCount, message.offset, message.velocity);
            }
            else if (message.effectType == EFFECT_SOUND_EVENT)
            {
                EnderUtilities.proxy.playSound(message.soundEventId, message.pitch, message.volume,
                        message.repeat, message.flags != 0, message.x, message.y, message.z);
            }
        }
    }
}
