package fi.dy.masa.enderutilities.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
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
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END || event.player == null)
        {
            return;
        }

        // Once every second
        if (++this.playerTickCounter >= 20)
        {
            this.playerTickCounter = 0;

            EntityPlayer player = event.player;
            ItemStack stack = player.getCurrentEquippedItem();

            if (stack != null && stack.getItem() instanceof IChunkLoadingItem)
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
                        ChunkLoading.getInstance().refreshChunkTimeout(target.dimension, target.posX >> 4, target.posZ >> 4);
                    }
                }
            }
        }
    }
}
