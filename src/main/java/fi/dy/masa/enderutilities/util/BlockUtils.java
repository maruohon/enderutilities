package fi.dy.masa.enderutilities.util;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.event.EntityEventHandler;
import fi.dy.masa.enderutilities.util.MethodHandleUtils.UnableToFindMethodHandleException;

public class BlockUtils
{
    public static final Pattern PATTERN_BLOCK_STATE_STRING = Pattern.compile("(?<name>([a-z0-9_]+:)?[a-z0-9\\._]+)\\[(?<props>[a-z0-9_]+=[a-z0-9_]+(,[a-z0-9_]+=[a-z0-9_]+)*)\\]");
    private static MethodHandle methodHandle_Block_getSilkTouchDrop;

    static
    {
        try
        {
            methodHandle_Block_getSilkTouchDrop = MethodHandleUtils.getMethodHandleVirtual(
                    Block.class, new String[] { "func_180643_i", "getSilkTouchDrop" }, IBlockState.class);
        }
        catch (UnableToFindMethodHandleException e)
        {
            EnderUtilities.logger.error("BlockUtils: Failed to get a MethodHandle for Block#getSilkTouchDrop()", e);
        }
    }

    public static Set<IBlockState> getMatchingBlockStatesForString(String blockStateString)
    {
        Set<IBlockState> validStates = new HashSet<>();
        ResourceLocation air = new ResourceLocation("minecraft:air");
        int index = blockStateString.indexOf('[');
        String name = index > 0 ? blockStateString.substring(0, index) : blockStateString;
        ResourceLocation key = new ResourceLocation(name);
        Block block = ForgeRegistries.BLOCKS.getValue(key);

        if (block != null && (block != Blocks.AIR || key.equals(air)))
        {
            // First get all valid states for this block
            Collection<IBlockState> statesTmp = block.getBlockState().getValidStates();
            // Then get the list of properties and their values in the given name (if any)
            List<Pair<String, String>> props = getBlockStatePropertiesFromString(blockStateString);

            // ... and then filter the list of all valid states by the provided properties and their values
            if (props.isEmpty() == false)
            {
                for (Pair<String, String> pair : props)
                {
                    statesTmp = getFilteredStates(statesTmp, pair.getLeft(), pair.getRight());
                }
            }

            validStates.addAll(statesTmp);
        }
        else
        {
            EnderUtilities.logger.warn("BlockUtils.getMatchingBlockStatesForString(): Invalid block state string '{}'", blockStateString);
        }

        return validStates;
    }

    public static List<Pair<String, String>> getBlockStatePropertiesFromString(String blockStateString)
    {
        Matcher matcherNameProps = PATTERN_BLOCK_STATE_STRING.matcher(blockStateString);

        if (matcherNameProps.matches())
        {
            List<Pair<String, String>> props = new ArrayList<>();
            // name[props]
            //String name = matcherNameProps.group("name");
            String propStr = matcherNameProps.group("props");
            String[] propParts = propStr.split(",");
            Pattern patternProp = Pattern.compile("(?<prop>[a-zA-Z0-9\\._-]+)=(?<value>[a-zA-Z0-9\\._-]+)");

            for (int i = 0; i < propParts.length; i++)
            {
                Matcher matcherProp = patternProp.matcher(propParts[i]);

                if (matcherProp.matches())
                {
                    props.add(Pair.of(matcherProp.group("prop"), matcherProp.group("value")));
                }
                else
                {
                    EnderUtilities.logger.warn("BlockUtils.getBlockStatePropertiesFromString(): Invalid block property '{}'", propParts[i]);
                }
            }

            Collections.sort(props); // the properties need to be in alphabetical order
            //System.out.printf("name: %s, props: %s (propStr: %s)\n", name, String.join(",", props), propStr);
            return props;
        }

        return Collections.emptyList();
    }

    public static <T extends Comparable<T>> List<IBlockState> getFilteredStates(Collection<IBlockState> initialStates, String propName, String propValue)
    {
        List<IBlockState> list = new ArrayList<>();

        for (IBlockState state : initialStates)
        {
            @SuppressWarnings("unchecked")
            IProperty<T> prop = (IProperty<T>) state.getBlock().getBlockState().getProperty(propName);

            if (prop != null)
            {
                Optional<T> value = prop.parseValue(propValue);

                if (value.isPresent() && state.getValue(prop).equals(value.get()))
                {
                    list.add(state);
                }
            }
        }

        return list;
    }

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
     * Checks if the player is allowed to change this block. Creative mode players are always allowed to change blocks.
     */
    public static boolean canChangeBlock(World world, BlockPos pos, EntityPlayer player, boolean allowTileEntities, float maxHardness)
    {
        if (player.capabilities.isCreativeMode)
        {
            return true;
        }

        IBlockState state = world.getBlockState(pos);

        if (state.getBlock().isAir(state, world, pos))
        {
            return true;
        }

        float hardness = state.getBlockHardness(world, pos);

        return world.isBlockModifiable(player, pos) && hardness >= 0 && (hardness <= maxHardness || state.getMaterial().isLiquid()) &&
               (allowTileEntities || world.getTileEntity(pos) == null);
    }

    public static void getDropAndSetToAir(World world, EntityPlayer player, BlockPos pos, EnumFacing side, boolean addToInventory)
    {
        if (player.capabilities.isCreativeMode == false)
        {
            ItemStack stack = BlockUtils.getPickBlockItemStack(world, pos, player, side);

            if (stack.isEmpty() == false)
            {
                if (addToInventory)
                {
                    IItemHandler inv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

                    if (inv != null)
                    {
                        stack = InventoryUtils.tryInsertItemStackToInventory(inv, stack);
                    }
                }

                if (stack.isEmpty() == false)
                {
                    EntityUtils.dropItemStacksInWorld(world, pos, stack, -1, true);
                }
            }
        }

        setBlockToAirWithBreakSound(world, pos);
    }

    public static void setBlockToAirWithBreakSound(World world, BlockPos pos)
    {
        playBlockBreakSound(world, pos);
        world.setBlockToAir(pos);
    }

    public static void playBlockBreakSound(World world, BlockPos pos)
    {
        IBlockState state = world.getBlockState(pos);
        SoundType soundtype = state.getBlock().getSoundType(state, world, pos, null);

        world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, soundtype.getVolume(), soundtype.getPitch());
    }

    public static void setBlockToAirWithoutSpillingContents(World world, BlockPos pos)
    {
        setBlockToAirWithoutSpillingContents(world, pos, 3);
    }

    public static void setBlockToAirWithoutSpillingContents(World world, BlockPos pos, int flags)
    {
        EntityEventHandler.setPreventItemSpawning(true);
        world.restoringBlockSnapshots = true;

        world.setBlockState(pos, Blocks.AIR.getDefaultState(), flags);

        world.restoringBlockSnapshots = false;
        EntityEventHandler.setPreventItemSpawning(false);
    }

    /**
     * Sets the block state in the world and plays the placement sound.
     * @return true if setting the block state succeeded
     */
    public static boolean setBlockStateWithPlaceSound(World world, BlockPos pos, IBlockState newState, int setBlockStateFlags)
    {
        boolean success = world.setBlockState(pos, newState, setBlockStateFlags);

        if (success)
        {
            SoundType soundtype = newState.getBlock().getSoundType(newState, world, pos, null);
            world.playSound(null, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        }

        return success;
    }

    public static ItemStack getPickBlockItemStack(World world, BlockPos pos, EntityPlayer player, EnumFacing side)
    {
        return getPickBlockItemStack(world, pos, world.getBlockState(pos), player, side);
    }

    /**
     * Gets a pick-blocked ItemStack for the given IBlockState <b>state</b>.
     * If the block currently in the world is different, the it will be replaced with <b>state</b>
     * for the duration of the pick-block operation.
     */
    public static ItemStack getPickBlockItemStack(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side)
    {
        IBlockState existingState = world.getBlockState(pos);
        NBTTagCompound nbt = null;
        boolean replaced = false;

        if (existingState.getBlock() != state.getBlock())
        {
            TileEntity te = world.getTileEntity(pos);

            if (te != null)
            {
                nbt = te.writeToNBT(new NBTTagCompound());
                te.onChunkUnload();
            }

            setBlockToAirWithoutSpillingContents(world, pos, 4);
            world.setBlockState(pos, state, 4);

            replaced = true;
        }

        RayTraceResult trace = new RayTraceResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), side, pos);
        ItemStack stack = state.getBlock().getPickBlock(state, trace, world, pos, player);

        if (replaced)
        {
            setBlockToAirWithoutSpillingContents(world, pos, 4);
            world.setBlockState(pos, existingState, 4);

            if (nbt != null)
            {
                TileUtils.createAndAddTileEntity(world, pos, nbt);
            }
        }

        return stack;
    }

    public static ItemStack getSilkTouchDrop(World world, BlockPos pos)
    {
        return getSilkTouchDrop(world.getBlockState(pos));
    }

    public static ItemStack getSilkTouchDrop(IBlockState state)
    {
        Block block = state.getBlock();
        ItemStack stack = ItemStack.EMPTY;

        try
        {
            stack = (ItemStack) methodHandle_Block_getSilkTouchDrop.invokeExact(block, state);
        }
        catch (Throwable t)
        {
            EnderUtilities.logger.warn("Error while trying invoke Block#getSilkTouchDrop() from {} via a MethodHandle", block.getClass().getName(), t);
        }

        return stack;
    }

    /**
     * Check if the given block can be placed in the given position.
     * Note: This method is a functional copy of ItemBlock.func_150936_a() which is client side only.
     */
    public static boolean checkCanPlaceBlockAt(World world, BlockPos pos, EnumFacing side, Block blockNew)
    {
        Block blockExisting = world.getBlockState(pos).getBlock();

        if (blockExisting == Blocks.SNOW_LAYER && blockExisting.isReplaceable(world, pos))
        {
            side = EnumFacing.UP;
        }
        else if (blockExisting.isReplaceable(world, pos) == false)
        {
            pos = pos.offset(side);
        }

        return world.mayPlace(blockNew, pos, false, side, (Entity) null);
    }
}
