package fi.dy.masa.enderutilities.event;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class TickHandler
{
    private int serverTickCounter;
    private int playerTickCounter;

    public void Tickhandler()
    {
        this.serverTickCounter = 0;
        this.playerTickCounter = 0;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        // Once every second
        if (++this.serverTickCounter >= 20)
        {
            this.serverTickCounter = 0;

            ChunkLoading.getInstance().tickChunkTimeouts();
        }

        ++this.playerTickCounter;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END || event.player == null)
        {
            return;
        }

        // Once every 2 seconds
        if (this.playerTickCounter % 40 == 0)
        {
            if (event.player.ridingEntity != null && event.player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness)))
            {
                ItemMobHarness.addAITask(event.player.ridingEntity, false);
            }

            ItemStack stack = event.player.getCurrentEquippedItem();
            if (stack == null)
            {
                return;
            }

            Item item = stack.getItem();
            if (item instanceof IChunkLoadingItem)
            {
                NBTTagCompound nbt = stack.getTagCompound();

                // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                if (nbt != null && nbt.getBoolean("ChunkLoadingRequired") == true)
                {
                    NBTHelperTarget target;

                    // Note: There is the possibility that the target or the selected link crystal
                    // has been changed since the chunk loading first started, but it just means
                    // that the refreshing will not happen, or will happen to the new target chunk,
                    // (the one currently active in the item) if that also happens to be chunk loaded by us.

                    // In case of modular items, we get the target info from the selected module (= Link Crystal)
                    if (stack.getItem() instanceof IModular)
                    {
                        target = NBTHelperTarget.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
                    }
                    else
                    {
                        target = NBTHelperTarget.getTargetFromItem(stack);
                    }

                    if (target != null)
                    {
                        ChunkLoading.getInstance().refreshChunkTimeout(target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4);
                    }
                }
            }
        }

        PlayerTaskScheduler.getInstance().runTasks(event.player.worldObj, event.player);
    }
}
