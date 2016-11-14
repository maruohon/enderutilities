package fi.dy.masa.enderutilities.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesPortal;
import fi.dy.masa.enderutilities.event.tasks.PlayerTaskScheduler;
import fi.dy.masa.enderutilities.item.ItemMobHarness;
import fi.dy.masa.enderutilities.item.base.IChunkLoadingItem;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EntityUtils;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

public class TickHandler
{
    private Set<UUID> portalFlags = new HashSet<UUID>();
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

        // This is currently only used for debug tasks, so let's disable it normally
        //TaskScheduler.getInstance().runTasks();

        ++this.playerTickCounter;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        if (event.side == Side.CLIENT || player.worldObj.isRemote)
        {
            return;
        }

        if (event.phase == TickEvent.Phase.START)
        {
            World world = player.worldObj;
            BlockPos pos = player.getPosition();
            IBlockState state = world.getBlockState(pos);
            UUID uuid = player.getUniqueID();

            if (state.getBlock() == EnderUtilitiesBlocks.blockPortal)
            {
                if (this.portalFlags.contains(uuid) == false)
                {
                    ((BlockEnderUtilitiesPortal) state.getBlock()).teleportEntity(world, pos, state, player);
                    this.portalFlags.add(uuid);
                }
            }
            else if (this.portalFlags.contains(uuid))
            {
                this.portalFlags.remove(uuid);
            }

            return;
        }

        // Once every 2 seconds
        if (this.playerTickCounter % 40 == 0)
        {
            if (player.isRiding() == true && player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.mobHarness)))
            {
                ItemMobHarness.addAITask(player.getRidingEntity(), false);
            }

            ItemStack stack = EntityUtils.getHeldItemOfType(player, IChunkLoadingItem.class);
            if (stack != null)
            {
                NBTTagCompound nbt = stack.getTagCompound();

                // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                if (nbt != null && nbt.getBoolean("ChunkLoadingRequired") == true)
                {
                    TargetData target;

                    // Note: There is the possibility that the target or the selected link crystal
                    // has been changed since the chunk loading first started, but it just means
                    // that the refreshing will not happen, or will happen to the new target chunk,
                    // (the one currently active in the item) if that also happens to be chunk loaded by us.

                    // In case of modular items, we get the target info from the selected module (= Link Crystal)
                    if (stack.getItem() instanceof IModular)
                    {
                        target = TargetData.getTargetFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
                    }
                    else
                    {
                        target = TargetData.getTargetFromItem(stack);
                    }

                    if (target != null)
                    {
                        ChunkLoading.getInstance().refreshChunkTimeout(target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4);
                    }
                }
            }
        }

        PlayerTaskScheduler.getInstance().runTasks(player.worldObj, player);
    }
}
