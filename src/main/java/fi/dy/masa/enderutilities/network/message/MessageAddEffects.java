package fi.dy.masa.enderutilities.network.message;

import fi.dy.masa.enderutilities.client.effects.Particles;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
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
	double x;
	double y;
	double z;

	public MessageAddEffects()
	{
	}

	public MessageAddEffects(int id, int flags, double x, double y, double z)
	{
		this.effectType = id;
		this.flags = flags;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.effectType = buf.readInt();
		this.flags = buf.readInt();
		this. x = buf.readDouble();
		this. y = buf.readDouble();
		this. z = buf.readDouble();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.effectType);
		buf.writeInt(this.flags);
		buf.writeDouble(this.x);
		buf.writeDouble(this.y);
		buf.writeDouble(this.z);
	}

	@Override
	public IMessage onMessage(MessageAddEffects message, MessageContext ctx)
	{
		EntityPlayer player;
		World world;
		//player = ctx.getServerHandler().playerEntity;
		//player = Minecraft.getMinecraft().thePlayer;
		//world = FMLClientHandler.instance().getWorldClient();
		player = FMLClientHandler.instance().getClientPlayerEntity();
		world = player.worldObj;

		if (player != null && world != null)
		{
			if (message.effectType == EFFECT_TELEPORT)
			{
				if ((message.flags & SOUND) == SOUND)
				{
					//world.playSoundEffect(message.x, message.y, message.z, "mob.endermen.portal", 0.8F, 1.0F + (world.rand.nextFloat() * 0.5f - world.rand.nextFloat() * 0.5f) * 0.5F);
				}
				if ((message.flags & PARTICLES) == PARTICLES)
				{
					System.out.println("particles!?" + " isRemote: " + world.isRemote);
					Particles.enderParticles(world, message.x, message.y, message.z, 32, 0.2d, 2.0d);
				}
			}
		}

		return null;
	}
}
