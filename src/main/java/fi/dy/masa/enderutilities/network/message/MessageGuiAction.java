package fi.dy.masa.enderutilities.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.ItemHandyBag;
import fi.dy.masa.enderutilities.item.ItemInventorySwapper;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilitiesInventory;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MessageGuiAction implements IMessage, IMessageHandler<MessageGuiAction, IMessage>
{
    private int guiId;
    private int action;
    private int elementId;
    private int dimension;
    private int posX;
    private int posY;
    private int posZ;

    public MessageGuiAction()
    {
    }

    public MessageGuiAction(int dim, int x, int y, int z, int guiId, int action, int elementId)
    {
        this.dimension = dim;
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        this.guiId = guiId;
        this.action = action;
        this.elementId = elementId;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.dimension = buf.readInt();
        this.posX = buf.readInt();
        this.posY = buf.readInt();
        this.posZ = buf.readInt();
        this.guiId = buf.readInt();
        this.action = buf.readShort();
        this.elementId = buf.readShort();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.dimension);
        buf.writeInt(this.posX);
        buf.writeInt(this.posY);
        buf.writeInt(this.posZ);
        buf.writeInt(this.guiId);
        buf.writeShort(this.action);
        buf.writeShort(this.elementId);
    }

    @Override
    public IMessage onMessage(MessageGuiAction message, MessageContext ctx)
    {
        EntityPlayer player = EnderUtilities.proxy.getPlayerFromMessageContext(ctx);
        World world = MinecraftServer.getServer().worldServerForDimension(message.dimension);

        if (player != null && world != null)
        {
            switch(message.guiId)
            {
                case ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC:
                    TileEntity te = world.getTileEntity(message.posX, message.posY, message.posZ);
                    if (te != null && te instanceof TileEntityEnderUtilitiesInventory)
                    {
                        ((TileEntityEnderUtilitiesInventory)te).performGuiAction(message.action, message.elementId);
                    }
                    break;
                case ReferenceGuiIds.GUI_ID_HANDY_BAG:
                    ItemHandyBag.performGuiAction(player, message.action, message.elementId);
                    break;
                case ReferenceGuiIds.GUI_ID_INVENTORY_SWAPPER:
                    ItemInventorySwapper.performGuiAction(player, message.action, message.elementId);
                    break;
                default:
            }
        }

        return null;
    }
}
