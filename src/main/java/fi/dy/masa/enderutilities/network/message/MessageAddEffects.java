package fi.dy.masa.enderutilities.network.message;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.effects.Particles;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageAddEffects implements IMessage, IMessageHandler<MessageAddEffects, IMessage>
{
    public static final int SOUND = 1;
    public static final int PARTICLES = 2;

    public static final int EFFECT_TELEPORT = 1;

    private int effectType;
    private int flags;
    private double x;
    private double y;
    private double z;
    private int particleCount;
    private double offset;
    private double velocity;

    public MessageAddEffects()
    {
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z, int particleCount, double offset, double velocity)
    {
        this.effectType = id;
        this.flags = flags;
        this.x = x;
        this.y = y;
        this.z = z;
        this.particleCount = particleCount;
        this.offset = offset;
        this.velocity = velocity;
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z, int particleCount)
    {
        this(id, flags, x, y, z, particleCount, 0.2d, 2.0d);
    }

    public MessageAddEffects(int id, int flags, double x, double y, double z)
    {
        this(id, flags, x, y, z, 32, 0.2d, 2.0d);
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.effectType = buf.readByte();
        this.flags = buf.readByte();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.particleCount = buf.readShort();
        this.offset = buf.readFloat();
        this.velocity = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte(this.effectType);
        buf.writeByte(this.flags);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeShort(this.particleCount);
        buf.writeFloat((float)this.offset);
        buf.writeFloat((float)this.velocity);
    }

    @Override
    public IMessage onMessage(MessageAddEffects message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        World world = player.worldObj;

        if (player != null && world != null)
        {
            if (message.effectType == EFFECT_TELEPORT)
            {
                if ((message.flags & SOUND) == SOUND)
                {
                    //world.playSoundEffect(message.x, message.y, message.z, "mob.endermen.portal", 0.8f, 1.0f + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5f);
                }
                if ((message.flags & PARTICLES) == PARTICLES)
                {
                    Particles.spawnParticles(world, "portal", message.x, message.y, message.z, message.particleCount, message.offset, message.velocity);
                }
            }
        }

        return null;
    }
}
