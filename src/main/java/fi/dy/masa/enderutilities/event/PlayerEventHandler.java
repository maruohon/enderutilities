package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.network.message.MessageKeyPressed;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.PlacementProperties;

public class PlayerEventHandler
{
    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        EntityPlayer player = event.getEntityPlayer();
        // You can only left click with the main hand, so this is fine here
        ItemStack stack = player.getHeldItemMainhand();
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EnumFacing face = event.getFace();

        if (stack.isEmpty() == false)
        {
            if (stack.getItem() == EnderUtilitiesItems.BUILDERS_WAND)
            {
                ((ItemBuildersWand) stack.getItem()).onLeftClickBlock(player, world, stack, pos, world.provider.getDimension(), face);
                event.setCanceled(true);
                return;
            }
            else if (stack.getItem() == EnderUtilitiesItems.RULER)
            {
                ((ItemRuler) stack.getItem()).onLeftClickBlock(player, world, stack, pos, world.provider.getDimension(), face);
                event.setCanceled(true);
                return;
            }
            else if (player.capabilities.isCreativeMode == false && stack.getItem() == EnderUtilitiesItems.ENDER_TOOL)
            {
                if (player.getCooldownTracker().hasCooldown(stack.getItem()))
                {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        if (world.getBlockState(pos).getBlock() == EnderUtilitiesBlocks.BARREL)
        {
            boolean takeItems = true;

            if (player.capabilities.isCreativeMode)
            {
                TileEntityBarrel te = BlockEnderUtilities.getTileEntitySafely(world, pos, TileEntityBarrel.class);

                if (te == null || te.getLabeledFaces().contains(face) == false)
                {
                    takeItems = false;
                }
            }

            if (takeItems)
            {
                if (world.isRemote)
                {
                    int dim = world.provider.getDimension();
                    int guiId = ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC;
                    int fullStacks = Configs.barrelInversedSneak == player.isSneaking() ? 1 : 0;

                    PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, pos, guiId, TileEntityBarrel.GUI_ACTION_TAKE_ITEMS, fullStacks));
                }

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRightClickAir(PlayerInteractEvent.RightClickItem event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            ItemStack stack = EntityUtils.getHeldItemOfType(event.getEntityPlayer(), EnderUtilitiesItems.BUILDERS_WAND);

            if (stack.isEmpty() == false && EnderUtilities.proxy.isControlKeyDown())
            {
                PacketHandler.INSTANCE.sendToServer(new MessageKeyPressed(HotKeys.KEYCODE_CUSTOM_1));
            }
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event)
    {
        ITextComponent message = event.getMessage();

        if (Configs.announceLocationBindingInChat == false && message instanceof TextComponentTranslation)
        {
            if ("enderutilities.chat.message.itemboundtolocation".equals(((TextComponentTranslation) message).getKey()))
            {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (event.player instanceof EntityPlayerMP)
        {
            PlacementProperties.getInstance().syncAllDataForPlayer((EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event)
    {
        PlayerTaskScheduler.getInstance().removeTask(event.player, null);
    }
}
