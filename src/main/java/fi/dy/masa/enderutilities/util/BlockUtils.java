package fi.dy.masa.enderutilities.util;

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

public class BlockUtils
{
    /*
    private static MethodHandle methodHandle_Block_createStackedBlock;

    static
    {
        try
        {
            methodHandle_Block_createStackedBlock = MethodHandleUtils.getMethodHandleVirtual(
                    Block.class, new String[] { "func_180643_i", "createStackedBlock" }, IBlockState.class);
        }
        catch (UnableToFindMethodHandleException e)
        {
            EnderUtilities.logger.error("BlockUtils: Failed to get a MethodHandle for Block#createStackedBlock()", e);
        }
    }
    */

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

    public static void setBlockToAirWithBreakSound(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        SoundType soundtype = state.getBlock().getSoundType(state, world, pos, null);

        world.setBlockToAir(pos);
        world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, soundtype.getVolume(), soundtype.getPitch());
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

    /*
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
            stack = (ItemStack) methodHandle_Block_createStackedBlock.invokeExact(block, state);
        }
        catch (Throwable t)
        {
            EnderUtilities.logger.warn("Error while trying invoke Block#createStackBlock() from {} via a MethodHandle", block.getClass().getName(), t);
        }

        return stack;
    }
    */

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
