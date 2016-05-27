package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemEnderBag;
import fi.dy.masa.enderutilities.item.ItemRuler;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class PlayerEventHandler
{
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event)
    {
        EntityPlayer player = event.getEntityPlayer();
        // You can only left click with the main hand, so this is fine here
        ItemStack stack = player.getHeldItemMainhand();
        if (stack == null)
        {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EnumFacing face = event.getFace();

        if (stack.getItem() == EnderUtilitiesItems.buildersWand)
        {
            ((ItemBuildersWand)stack.getItem()).onLeftClickBlock(player, world, stack, pos, player.dimension, face);
            event.setCanceled(true);
        }
        else if (stack.getItem() == EnderUtilitiesItems.ruler)
        {
            ((ItemRuler)stack.getItem()).onLeftClickBlock(player, world, stack, pos, player.dimension, face);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPlayerOpenContainer(PlayerOpenContainerEvent event)
    {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = EntityUtils.getHeldItemOfType(player, EnderUtilitiesItems.enderBag);
        if (stack == null)
        {
            return;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.getBoolean("IsOpen") == true)
        {
            if (player.openContainer != player.inventoryContainer
                && (ItemEnderBag.targetNeedsToBeLoadedOnClient(stack) == false
                || ItemEnderBag.targetOutsideOfPlayerRange(stack, player) == false))
            {
                // Allow access from anywhere with the Ender Bag (bypassing the distance checks)
                event.setResult(Result.ALLOW);
            }
            // Ender Bag: Player has just closed the remote container
            else if (player.worldObj.isRemote == false)
            {
                nbt.removeTag("ChunkLoadingRequired");
                nbt.removeTag("IsOpen");
                player.inventory.markDirty();
            }

            if (player instanceof EntityPlayerMP)
            {
                player.inventoryContainer.detectAndSendChanges();
            }
        }
    }
}
