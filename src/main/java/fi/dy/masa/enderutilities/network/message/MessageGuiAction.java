package fi.dy.masa.enderutilities.network.message;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesSided;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGuiAction implements IMessage, IMessageHandler<MessageGuiAction, IMessage>
{
    private int guiId;
    private int elementId;
    private short action;
    private int dimension;
    private int posX;
    private int posY;
    private int posZ;

    public MessageGuiAction()
    {
    }

    public MessageGuiAction(int dim, int x, int y, int z, int guiId, int element, short action)
    {
        this.guiId = guiId;
        this.elementId = element;
        this.action = action;
        this.dimension = dim;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.dimension = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.guiId = buf.readInt();
        this.elementId = buf.readInt();
        this.action = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.dimension);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.guiId);
        buf.writeInt(this.elementId);
        buf.writeShort(this.action);
    }

    @Override
    public IMessage onMessage(MessageGuiAction message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        //World world = player.worldObj;
        World world = MinecraftServer.getServer().worldServerForDimension(message.dimension);

        if (player != null && world != null)
        {
            switch(message.guiId)
            {
                case ReferenceGuiIds.GUI_ID_ENDER_FURNACE:
                    TileEntity te = world.getTileEntity(new BlockPos(message.posX, message.posY, message.posZ));
                    if (te != null && te instanceof TileEntityEnderUtilitiesSided)
                    {
                        ((TileEntityEnderUtilitiesSided)te).performGuiAction(message.elementId, message.action);
                    }
                    break;
                default:
            }
        }

        return null;
    }
}
