package fi.dy.masa.enderutilities.util;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderUtilities;

public class BlockUtils
{
    /**
     * Breaks the block as a player, and thus drops the item(s) from it
     */
    public static void breakBlockAsPlayer(World world, BlockPos pos, IBlockState stateExisting, EntityPlayerMP playerMP, ItemStack toolStack)
    {
        PlayerInteractionManager manager = playerMP.interactionManager;
        int exp = ForgeHooks.onBlockBreakEvent(world, manager.getGameType(), playerMP, pos);
        if (exp != -1)
        {
            Block blockExisting = stateExisting.getBlock();

            blockExisting.onBlockHarvested(world, pos, stateExisting, playerMP);
            boolean harvest = blockExisting.removedByPlayer(stateExisting, world, pos, playerMP, true);

            if (harvest)
            {
                blockExisting.onBlockDestroyedByPlayer(world, pos, stateExisting);
                blockExisting.harvestBlock(world, playerMP, pos, stateExisting, null, toolStack);
            }
        }
    }

    /**
     * Sets the block state in the world and plays the placement sound
     */
    public static void placeBlock(World world, BlockPos pos, IBlockState newState, int setBlockStateFlags)
    {
        world.setBlockState(pos, newState, setBlockStateFlags);

        SoundType soundtype = newState.getBlock().getSoundType();
        world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
    }

    /**
     * Check if the block in the world matches the one asked for.
     * Use OreDictionary.WILDCARD_VALUE for the requiredMeta to ignore block meta.
     * Use null for the TEClass if the block doesn't have a TileEntity.
     * If the block has a TE of type TileEntityEnderUtilities but the orientation doesn't matter,
     * then give ForgeDirection.UNKNOWN as the requiredOrientation.
     */
    public static boolean blockMatches(World world, BlockPos pos, Block requiredBlock, int requiredMeta, Class <? extends TileEntity> TEClass, EnumFacing requiredOrientation)
    {
        IBlockState iBlockState = world.getBlockState(pos);
        Block block = iBlockState.getBlock();
        int meta = block.getMetaFromState(iBlockState);
        TileEntity te = world.getTileEntity(pos);

        if (block != requiredBlock || (meta != requiredMeta && requiredMeta != OreDictionary.WILDCARD_VALUE))
        {
            return false;
        }

        if (te == null && TEClass == null)
        {
            return true;
        }

        if (te != null && TEClass != null && TEClass.isAssignableFrom(te.getClass()) == true && (requiredOrientation == null
            || (te instanceof TileEntityEnderUtilities && ((TileEntityEnderUtilities)te).getFacing() == requiredOrientation)))
        {
            return true;
        }

        return false;
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
