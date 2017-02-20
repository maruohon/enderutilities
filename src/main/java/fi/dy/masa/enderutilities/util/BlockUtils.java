package fi.dy.masa.enderutilities.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import fi.dy.masa.enderutilities.EnderUtilities;

public class BlockUtils
{
    /**
     * Breaks the block as a player, and thus drops the item(s) from it
     */
    public static void breakBlockAsPlayer(World world, BlockPos pos, EntityPlayerMP playerMP, ItemStack toolStack)
    {
        PlayerInteractionManager manager = playerMP.interactionManager;
        int exp = ForgeHooks.onBlockBreakEvent(world, manager.getGameType(), playerMP, pos);
        if (exp != -1)
        {
            IBlockState stateExisting = world.getBlockState(pos);
            Block blockExisting = stateExisting.getBlock();

            blockExisting.onBlockHarvested(world, pos, stateExisting, playerMP);
            boolean harvest = blockExisting.removedByPlayer(stateExisting, world, pos, playerMP, true);

            if (harvest)
            {
                blockExisting.onBlockDestroyedByPlayer(world, pos, stateExisting);
                blockExisting.harvestBlock(world, playerMP, pos, stateExisting, world.getTileEntity(pos), toolStack);
            }
        }
    }

    /**
     * Sets the block state in the world and plays the placement sound
     */
    public static void placeBlock(World world, BlockPos pos, IBlockState newState, int setBlockStateFlags)
    {
        world.setBlockState(pos, newState, setBlockStateFlags);

        SoundType soundtype = newState.getBlock().getSoundType(newState, world, pos, null);
        world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
    }

    public static ItemStack getStackedItemFromBlock(World world, BlockPos pos, EntityPlayer player, EnumFacing side)
    {
        IBlockState state = world.getBlockState(pos);
        return getStackedItemFromBlock(world, pos, state, player, side);
    }

    public static ItemStack getStackedItemFromBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side)
    {
        RayTraceResult trace = new RayTraceResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), side, pos);
        return state.getBlock().getPickBlock(state, trace, world, pos, player);
    }

    public static ItemStack getStackedItemFromBlock(World world, BlockPos pos)
    {
        return getStackedItemFromBlock(world.getBlockState(pos));
    }

    public static ItemStack getStackedItemFromBlock(IBlockState state)
    {
        Block block = state.getBlock();
        ItemStack stack = null;

        try
        {
            Method method = ReflectionHelper.findMethod(Block.class, block, new String[] { "func_180643_i", "createStackedBlock" }, IBlockState.class);
            stack = (ItemStack) method.invoke(block, state);
        }
        catch (UnableToFindMethodException e)
        {
            EnderUtilities.logger.error("Error while trying reflect Block#createStackBlock() from {} (UnableToFindMethodException)", block.getClass().getSimpleName());
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            EnderUtilities.logger.error("Error while trying reflect Block#createStackBlock() from {} (InvocationTargetException)", block.getClass().getSimpleName());
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            EnderUtilities.logger.error("Error while trying reflect Block#createStackBlock() from {} (IllegalAccessException)", block.getClass().getSimpleName());
            e.printStackTrace();
        }

        return stack;
    }

    /**
     * Check if the block in the given ItemStack stack can be placed in the given position.
     * Note: This method is a functional copy of ItemBlock.func_150936_a() which is client side only.
     */
    public static boolean checkCanPlaceBlockAt(World world, BlockPos pos, EnumFacing side, Block blockNew, ItemStack stack)
    {
        if (stack == null)
        {
            return false;
        }

        Block blockExisting = world.getBlockState(pos).getBlock();

        if (blockExisting == Blocks.SNOW_LAYER && blockExisting.isReplaceable(world, pos) == true)
        {
            side = EnumFacing.UP;
        }
        else if (blockExisting.isReplaceable(world, pos) == false)
        {
            pos = pos.offset(side);
        }

        return world.canBlockBePlaced(blockNew, pos, false, side, (Entity)null, stack);
    }
}
