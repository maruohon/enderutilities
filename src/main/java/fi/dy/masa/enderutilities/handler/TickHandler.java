package fi.dy.masa.enderutilities.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class TickHandler
{
    public void Tickhandler()
    {
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            return;
        }

        ChunkLoading.getInstance().tickChunkTimeouts();
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        if (event.side == Side.CLIENT || event.phase == TickEvent.Phase.END || event.player == null)
        {
            return;
        }

        EntityPlayer player = event.player;
        ItemStack stack = player.getCurrentEquippedItem();

        if (stack != null && stack.getItem() instanceof IChunkLoadingItem)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt != null)
            {
                // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                if (nbt.hasKey("ChunkLoadingRequired") == true && nbt.getBoolean("ChunkLoadingRequired") == true)
                {
                    NBTHelperTarget target = new NBTHelperTarget();
                    if (target.readTargetTagFromNBT(nbt) != null)
                    {
                        ChunkLoading.getInstance().refreshChunkTimeout(target.dimension, target.posX >> 4, target.posZ >> 4);
                    }
                }
            }
        }
    }
}
