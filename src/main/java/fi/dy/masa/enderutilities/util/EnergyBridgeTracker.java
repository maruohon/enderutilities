package fi.dy.masa.enderutilities.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class EnergyBridgeTracker
{
    private static final Set<BlockPosEU> BRIDGE_LOCATIONS = new HashSet<>();
    private static final Int2IntOpenHashMap BRIDGE_COUNTS = new Int2IntOpenHashMap();
    private static int bridgeCountInEndDimensions;
    private static boolean dirty = false;

    public static void addBridgeLocation(World world, BlockPos pos)
    {
        addBridgeLocation(new BlockPosEU(pos, world.provider.getDimension(), EnumFacing.UP), true);

        if (WorldUtils.isEndDimension(world))
        {
            bridgeCountInEndDimensions++;
        }
    }

    public static void removeBridgeLocation(World world, BlockPos posIn)
    {
        final int dimension = world.provider.getDimension();
        BlockPosEU pos = new BlockPosEU(posIn, dimension, EnumFacing.UP);

        if (BRIDGE_LOCATIONS.remove(pos))
        {
            if (BRIDGE_COUNTS.get(dimension) <= 1)
            {
                BRIDGE_COUNTS.remove(dimension);
            }
            else
            {
                BRIDGE_COUNTS.addTo(dimension, -1);
            }

            if (WorldUtils.isEndDimension(world))
            {
                bridgeCountInEndDimensions--;
            }
        }

        dirty = true;
    }

    public static boolean dimensionHasEnergyBridge(int dimension)
    {
        return BRIDGE_COUNTS.getOrDefault(dimension, 0) > 0;
    }

    public static boolean endHasEnergyBridges()
    {
        return bridgeCountInEndDimensions > 0;
    }

    private static void addBridgeLocation(BlockPosEU pos, boolean markDirty)
    {
        if (BRIDGE_LOCATIONS.contains(pos) == false)
        {
            BRIDGE_LOCATIONS.add(pos);
            BRIDGE_COUNTS.addTo(pos.getDimension(), 1);
        }

        if (markDirty)
        {
            dirty = true;
        }
    }

    /**
     * Reads the Energy Bridge locations from NBT and adds them to the list.
     * NOTE: Does NOT clear the list before adding the new locations to it.
     * @param nbt
     */
    public static void readFromNBT(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("EnergyBridges", Constants.NBT.TAG_LIST) == false)
        {
            return;
        }

        NBTTagList tagList = nbt.getTagList("EnergyBridges", Constants.NBT.TAG_COMPOUND);
        int count = tagList.tagCount();
        boolean hasEndBridgeCount = nbt.hasKey("BridgeCountInEnd", Constants.NBT.TAG_INT);
        bridgeCountInEndDimensions = nbt.getInteger("BridgeCountInEnd");

        for (int i = 0; i < count; ++i)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);

            if (tag.hasKey("Dim", Constants.NBT.TAG_INT) &&
                tag.hasKey("posX", Constants.NBT.TAG_INT) &&
                tag.hasKey("posY", Constants.NBT.TAG_INT) &&
                tag.hasKey("posZ", Constants.NBT.TAG_INT))
            {
                int dim = tag.getInteger("Dim");

                addBridgeLocation(new BlockPosEU(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"),
                        dim, EnumFacing.UP), false);

                // Backwards compatibility for reading data from before the end bridge count was saved
                if (hasEndBridgeCount == false && dim == 1)
                {
                    bridgeCountInEndDimensions++;
                }
            }
        }
    }

    public static NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (BRIDGE_LOCATIONS.isEmpty() == false)
        {
            NBTTagList tagList = new NBTTagList();

            for (BlockPosEU pos : BRIDGE_LOCATIONS)
            {
                if (pos != null)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setInteger("Dim", pos.getDimension());
                    tag.setInteger("posX", pos.getX());
                    tag.setInteger("posY", pos.getY());
                    tag.setInteger("posZ", pos.getZ());
                    tagList.appendTag(tag);
                }
            }

            if (tagList.tagCount() > 0)
            {
                nbt.setTag("EnergyBridges", tagList);
            }

            nbt.setInteger("BridgeCountInEnd", bridgeCountInEndDimensions);
        }

        return nbt;
    }

    public static void readFromDisk()
    {
        // Clear the data structures when reading the data for a world/save, so that valid Energy Bridges
        // from another world won't carry over to a world/save that doesn't have the file yet.
        BRIDGE_LOCATIONS.clear();
        BRIDGE_COUNTS.clear();
        bridgeCountInEndDimensions = 0;

        try
        {
            File saveDir = DimensionManager.getCurrentSaveRootDirectory();

            if (saveDir == null)
            {
                return;
            }

            File file = new File(new File(saveDir, Reference.MOD_ID), "energybridges.dat");

            if (file.exists() && file.isFile())
            {
                FileInputStream is = new FileInputStream(file);
                readFromNBT(CompressedStreamTools.readCompressed(is));
                is.close();
            }
        }
        catch (Exception e)
        {
            EnderUtilities.logger.warn("Failed to read Energy Bridge data from file!", e);
        }
    }

    public static void writeToDisk()
    {
        if (dirty == false)
        {
            return;
        }

        try
        {
            File saveDir = DimensionManager.getCurrentSaveRootDirectory();

            if (saveDir == null)
            {
                return;
            }

            saveDir = new File(saveDir, Reference.MOD_ID);

            if (saveDir.exists() == false)
            {
                if (saveDir.mkdirs() == false)
                {
                    EnderUtilities.logger.warn("Failed to create the save directory '" + saveDir + "'");
                    return;
                }
            }

            File fileTmp = new File(saveDir, "energybridges.dat.tmp");
            File fileReal = new File(saveDir, "energybridges.dat");
            FileOutputStream os = new FileOutputStream(fileTmp);
            CompressedStreamTools.writeCompressed(writeToNBT(new NBTTagCompound()), os);
            os.close();

            if (fileReal.exists())
            {
                fileReal.delete();
            }

            fileTmp.renameTo(fileReal);
            dirty = false;
        }
        catch (Exception e)
        {
            EnderUtilities.logger.warn("Failed to write Energy Bridge data to file!", e);
        }
    }
}
