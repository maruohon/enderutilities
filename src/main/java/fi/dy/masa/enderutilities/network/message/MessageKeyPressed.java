package fi.dy.masa.enderutilities.network.message;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IKeyBoundUnselected;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.tileentity.TileEntityElevator;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import io.netty.buffer.ByteBuf;

public class MessageKeyPressed implements IMessage
{
    private int keyPressed;

    public MessageKeyPressed()
    {
    }

    public MessageKeyPressed(int key)
    {
        this.keyPressed = key;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.keyPressed = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.keyPressed);
    }

    public static class Handler implements IMessageHandler<MessageKeyPressed, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageKeyPressed message, MessageContext ctx)
        {
            if (ctx.side != Side.SERVER)
            {
                EnderUtilities.logger.error("Wrong side in MessageKeyPressed: " + ctx.side);
                return null;
            }

            final EntityPlayerMP sendingPlayer = ctx.getServerHandler().player;
            if (sendingPlayer == null)
            {
                EnderUtilities.logger.error("Sending player was null in MessageKeyPressed");
                return null;
            }

            final WorldServer playerWorldServer = sendingPlayer.getServerWorld();
            if (playerWorldServer == null)
            {
                EnderUtilities.logger.error("World was null in MessageKeyPressed");
                return null;
            }

            playerWorldServer.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    processMessage(message, sendingPlayer);
                }
            });

            return null;
        }

        protected void processMessage(final MessageKeyPressed message, EntityPlayer player)
        {
            if (this.handleEnderElevator(message, player))
            {
                return;
            }

            ItemStack stack = EntityUtils.getHeldItemOfType(player, IKeyBound.class);

            if (stack.isEmpty() == false &&
                ((IKeyBound) stack.getItem()).doKeyBindingAction(player, stack, message.keyPressed))
            {
                return;
            }

            stack = InventoryUtils.getFirstItemOfType(player, IKeyBoundUnselected.class);

            if (stack.isEmpty() == false &&
                ((IKeyBoundUnselected) stack.getItem()).doUnselectedKeyAction(player, stack, message.keyPressed))
            {
                return;
            }

            this.handleKeysGeneric(message.keyPressed, player);
        }

        protected boolean handleEnderElevator(final MessageKeyPressed message, EntityPlayer player)
        {
            if (message.keyPressed == HotKeys.KEYCODE_JUMP || message.keyPressed == HotKeys.KEYCODE_SNEAK)
            {
                BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                World world = player.getEntityWorld();
                TileEntityElevator te = BlockEnderUtilitiesTileEntity.getTileEntitySafely(world, pos, TileEntityElevator.class);

                if (te == null)
                {
                    te = BlockEnderUtilitiesTileEntity.getTileEntitySafely(world, pos.down(), TileEntityElevator.class);
                }

                if (te != null)
                {
                    te.activateForEntity(player, message.keyPressed == HotKeys.KEYCODE_JUMP);
                    return true;
                }
            }

            return false;
        }

        private boolean handleKeysGeneric(int key, EntityPlayer player)
        {
            World world = player.getEntityWorld();
            RayTraceResult trace = EntityUtils.getRayTraceFromPlayer(world, player, true);

            if (trace != null && trace.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                BlockPos pos = trace.getBlockPos();
                TileEntityEnderUtilities te = BlockEnderUtilities.getTileEntitySafely(world, pos, TileEntityEnderUtilities.class);

                if (te != null)
                {
                    return te.onInputAction(key, player, trace, world, pos);
                }
            }

            return false;
        }
    }
}
