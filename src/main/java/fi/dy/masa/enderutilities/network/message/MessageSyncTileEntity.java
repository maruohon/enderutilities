package fi.dy.masa.enderutilities.network.message;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import io.netty.buffer.ByteBuf;

public class MessageSyncTileEntity implements IMessage
{
    private int header;
    private BlockPos pos;
    private int[] intValues;
    private ItemStack[] stacks;

    public MessageSyncTileEntity()
    {
    }

    /**
     * Sends up to 15 integer values
     * @param dataValue
     */
    public MessageSyncTileEntity(BlockPos pos, int... intValues)
    {
        this.pos = pos;
        this.intValues = intValues;
        this.stacks = new ItemStack[0];
    }

    /**
     * Sends up to 15 ItemStacks
     * @param dataValue
     */
    public MessageSyncTileEntity(BlockPos pos, ItemStack... stacks)
    {
        this.pos = pos;
        this.intValues = new int[0];
        this.stacks = stacks;
    }

    /**
     * Sends one integer and one ItemStack
     * @param dataValue
     */
    public MessageSyncTileEntity(BlockPos pos, int intValue, ItemStack stack)
    {
        this.pos = pos;
        this.intValues = new int[] { intValue };
        this.stacks = new ItemStack[] { stack };
    }

    /**
     * Sends up to 15 integers and up to 15 ItemStacks
     * @param pos
     * @param intValues
     * @param stacks
     */
    public MessageSyncTileEntity(BlockPos pos, int[] intValues, ItemStack[] stacks)
    {
        this.pos = pos;
        this.intValues = intValues;
        this.stacks = stacks;
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeByte((this.stacks.length & 0xF) << 4 | (this.intValues.length & 0xF));

        buf.writeInt(this.pos.getX());
        buf.writeByte((byte) (this.pos.getY() & 0xFF));
        buf.writeInt(this.pos.getZ());

        for (int i = 0; i < (this.intValues.length & 0xF); i++)
        {
            buf.writeInt(this.intValues[i]);
        }

        for (int i = 0; i < (this.stacks.length & 0xF); i++)
        {
            ByteBufUtilsEU.writeItemStackToBuffer(buf, this.stacks[i]);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.header = buf.readByte();

        this.pos = new BlockPos(buf.readInt(), ((int) buf.readByte()) & 0xFF, buf.readInt());

        try
        {
            int len = (this.header & 0x0F);
            this.intValues = new int[len];

            for (int i = 0; i < len; i++)
            {
                this.intValues[i] = buf.readInt();
            }

            len = (this.header & 0xF0) >>> 4;
            this.stacks = new ItemStack[len];

            for (int i = 0; i < len; i++)
            {
                this.stacks[i] = ByteBufUtilsEU.readItemStackFromBuffer(buf);
            }
        }
        catch (IOException e)
        {
            EnderUtilities.logger.warn("MessageSyncTileEntity: Exception while reading data from buffer", e);
        }
    }

    public static class Handler implements IMessageHandler<MessageSyncTileEntity, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageSyncTileEntity message, MessageContext ctx)
        {
            if (ctx.side != Side.CLIENT)
            {
                EnderUtilities.logger.error("Wrong side in MessageSyncTileEntity: " + ctx.side);
                return null;
            }

            Minecraft mc = FMLClientHandler.instance().getClient();
            final EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);

            if (mc == null || player == null)
            {
                EnderUtilities.logger.error("Minecraft or player was null in MessageSyncTileEntity");
                return null;
            }

            mc.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message, player);
                }
            });

            return null;
        }

        protected void processMessage(final MessageSyncTileEntity message, EntityPlayer player)
        {
            World world = player.getEntityWorld();
            TileEntity te = world.getTileEntity(message.pos);

            if (te instanceof ISyncableTile)
            {
                ISyncableTile syncable = (ISyncableTile) te;
                syncable.syncTile(message.intValues, message.stacks);
            }
        }
    }
}
