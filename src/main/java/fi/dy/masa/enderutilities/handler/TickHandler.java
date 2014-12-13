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
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

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
                if (stack.getTagCompound() != null)
                {
                    NBTTagCompound nbt = stack.getTagCompound();

                    // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                    if (nbt.hasKey("ChunkLoadingRequired") == true && nbt.getBoolean("ChunkLoadingRequired") == true)
                    {
                        // In case of modular items, we get the target info from the selected module (= Link Crystal)
                        if (stack.getItem() instanceof IModular)
                        {
                            ItemStack moduleStack = ((IModular)stack.getItem()).getSelectedModuleStack(stack, UtilItemModular.ModuleType.TYPE_LINKCRYSTAL);
                            if (moduleStack != null)
                            {
                                nbt = moduleStack.getTagCompound();
                            }
                        }

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
}
