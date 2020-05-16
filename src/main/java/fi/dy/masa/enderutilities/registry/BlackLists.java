package fi.dy.masa.enderutilities.registry;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.util.BlockUtils;

public class BlackLists
{
    private static final Set<String> ENDER_BAG_BLACKLIST_NAMES = new HashSet<>();
    private static final Set<String> ENDER_BAG_WHITELIST_NAMES = new HashSet<>();
    private static final Set<Block> ENDER_BAG_BLACKLIST_BLOCKS = new HashSet<>();
    private static final Set<Block> ENDER_BAG_WHITELIST_BLOCKS = new HashSet<>();
    private static final Set<IBlockState> DOLLY_BLACKLIST = new HashSet<>();
    private static final Set<IBlockState> ENERGY_BRIDGE_BEDROCK_WHITELIST = new HashSet<>();
    private static final Set<Class<? extends Entity>> TELEPORT_BLACKLIST_CLASSES = new HashSet<>();

    public static void registerDollyBlacklist(String[] list)
    {
        DOLLY_BLACKLIST.clear();

        for (String name : list)
        {
            Set<IBlockState> states = BlockUtils.getMatchingBlockStatesForString(name);

            if (states.isEmpty() == false)
            {
                DOLLY_BLACKLIST.addAll(states);
            }
            else
            {
                EnderUtilities.logger.warn("Invalid block state '{}' in the dolly block blacklist", name);
            }
        }
    }

    public static void registerEnergyBridgeBedrockWhitelist(String[] whitelist)
    {
        ENERGY_BRIDGE_BEDROCK_WHITELIST.clear();

        for (String name : whitelist)
        {
            Set<IBlockState> states = BlockUtils.getMatchingBlockStatesForString(name);

            if (states.isEmpty() == false)
            {
                ENERGY_BRIDGE_BEDROCK_WHITELIST.addAll(states);
            }
            else
            {
                EnderUtilities.logger.warn("Invalid block state '{}' in the energy bridge bedrock whitelist", name);
            }
        }
    }

    public static void registerEnderBagLists(String[] blacklist, String[] whitelist)
    {
        // TODO Support block states instead of blocks

        ENDER_BAG_BLACKLIST_NAMES.clear();
        ENDER_BAG_BLACKLIST_BLOCKS.clear();

        for (String entry : blacklist)
        {
            ENDER_BAG_BLACKLIST_NAMES.add(entry);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));

            if (block != null && block != Blocks.AIR)
            {
                ENDER_BAG_BLACKLIST_BLOCKS.add(block);
            }
        }

        ENDER_BAG_WHITELIST_NAMES.clear();
        ENDER_BAG_WHITELIST_BLOCKS.clear();

        for (String entry : whitelist)
        {
            ENDER_BAG_WHITELIST_NAMES.add(entry);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry));

            if (block != null && block != Blocks.AIR)
            {
                ENDER_BAG_BLACKLIST_BLOCKS.add(block);
            }
        }
    }

    public static boolean isBlockAllowedForDolly(IBlockState state)
    {
        return DOLLY_BLACKLIST.contains(state) == false;
    }

    public static boolean isBlockAllowedForEnderBag(World world, BlockPos pos)
    {
        if (Configs.enderBagListTypeIsWhitelist)
        {
            return ENDER_BAG_WHITELIST_BLOCKS.contains(world.getBlockState(pos).getBlock());
        }
        else
        {
            return ENDER_BAG_BLACKLIST_BLOCKS.contains(world.getBlockState(pos).getBlock()) == false;
        }
    }

    public static boolean isBlockAllowedForEnderBag(String blockName)
    {
        if (Configs.enderBagListTypeIsWhitelist)
        {
            return ENDER_BAG_WHITELIST_NAMES.contains(blockName);
        }
        else
        {
            return ENDER_BAG_BLACKLIST_NAMES.contains(blockName) == false;
        }
    }

    public static boolean isBlockValidBedrockForEnergyBridge(IBlockState state)
    {
        return ENERGY_BRIDGE_BEDROCK_WHITELIST.contains(state);
    }

    public static void registerTeleportBlacklist(String[] blacklist)
    {
        TELEPORT_BLACKLIST_CLASSES.clear();

        for (String name : blacklist)
        {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name));

            if (entry != null && entry.getEntityClass() != null)
            {
                TELEPORT_BLACKLIST_CLASSES.add(entry.getEntityClass());
            }
            else
            {
                EnderUtilities.logger.warn("Unknown Entity type '{}' on the teleport blacklist", name);
            }
        }
    }

    public static boolean isEntityBlacklistedForTeleport(Entity entity)
    {
        return (entity instanceof MultiPartEntityPart && TELEPORT_BLACKLIST_CLASSES.contains(EntityDragon.class)) ||
                TELEPORT_BLACKLIST_CLASSES.contains(entity.getClass());
    }
}
