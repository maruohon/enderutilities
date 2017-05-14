package fi.dy.masa.enderutilities.event;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
    private static TickHandler instance;
    private Set<UUID> portalFlags = new HashSet<UUID>();
    private int serverTickCounter;
    private int playerTickCounter;

    public TickHandler()
    {
        instance = this;
    }

    public static TickHandler instance()
    {
        return instance;
    }

    public void addPlayerToTeleport(EntityPlayer player)
    {
        this.portalFlags.add(player.getUniqueID());
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

        this.teleportPlayers();
        ++this.playerTickCounter;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event)
    {
        EntityPlayer player = event.player;

        if (event.side == Side.CLIENT || player.getEntityWorld().isRemote)
        {
            return;
        }

        // Once every 2 seconds
        if (this.playerTickCounter % 40 == 0)
        {
            if (player.isRiding() && player.inventory.hasItemStack(new ItemStack(EnderUtilitiesItems.MOB_HARNESS)))
            {
                ItemMobHarness.addAITask(player.getRidingEntity(), false);
            }

            ItemStack stack = EntityUtils.getHeldItemOfType(player, IChunkLoadingItem.class);

            if (stack.isEmpty() == false)
            {
                NBTTagCompound nbt = stack.getTagCompound();

                // If the player is holding an item that requires a chunk to stay loaded, refresh the timeout value
                if (nbt != null && nbt.getBoolean("ChunkLoadingRequired"))
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

        PlayerTaskScheduler.getInstance().runTasks(player.getEntityWorld(), player);
    }

    private void teleportPlayers()
    {
        PlayerList list = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

        if (this.portalFlags.isEmpty() == false)
        {
            for (UUID uuid : this.portalFlags)
            {
                EntityPlayer player = list.getPlayerByUUID(uuid);

                if (player != null)
                {
                    World world = player.getEntityWorld();
                    BlockPos pos = player.getPosition();

                    // The exact intersection is checked here, because the players get added
                    // to the set when they enter the block space, even if they don't intersect with the portal yet.
                    for (int i = 0; i < 3; i++)
                    {
                        IBlockState state = world.getBlockState(pos);

                        if (state.getBlock() == EnderUtilitiesBlocks.PORTAL &&
                            player.getEntityBoundingBox().intersectsWith(state.getBoundingBox(world, pos).offset(pos)))
                        {
                            ((BlockEnderUtilitiesPortal) state.getBlock()).teleportEntity(world, pos, state, player);
                            break;
                        }

                        pos = pos.up();
                    }
                }
            }

            this.portalFlags.clear();
        }
    }
}
