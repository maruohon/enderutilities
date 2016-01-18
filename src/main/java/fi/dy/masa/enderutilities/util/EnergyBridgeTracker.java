package fi.dy.masa.enderutilities.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EnergyBridgeTracker
{
    private static boolean dirty = false;
    private static List<BlockPosEU> bridgeLocations = new ArrayList<BlockPosEU>();
    private static TIntObjectHashMap<Integer> bridgeCounts = new TIntObjectHashMap<Integer>();

    public static void addBridgeLocation(BlockPosEU pos)
    {
        addBridgeLocation(pos, true);
    }

    private static void addBridgeLocation(BlockPosEU pos, boolean markDirty)
    {
        Integer count = bridgeCounts.get(pos.dimension);
        if (count == null)
        {
            count = Integer.valueOf(0);
        }

        bridgeCounts.put(pos.dimension, Integer.valueOf(count.intValue() + 1));

        if (bridgeLocations.contains(pos) == false)
        {
            bridgeLocations.add(pos);
        }

        if (markDirty == true)
        {
            dirty = true;
        }
    }

    public static void removeBridgeLocation(BlockPosEU pos)
    {
        Integer count = bridgeCounts.get(pos.dimension);
        if (count == null)
        {
            count = Integer.valueOf(1);
        }

        if (bridgeLocations.remove(pos) == true)
        {
            bridgeCounts.put(pos.dimension, Integer.valueOf(count.intValue() - 1));
        }

        dirty = true;
    }

    public static boolean dimensionHasEnergyBridge(int dimension)
    {
        Integer count = bridgeCounts.get(dimension);
        return count != null && count.intValue() > 0;
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
        for (int i = 0; i < count; ++i)
        {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            if (tag.hasKey("Dim", Constants.NBT.TAG_INT) && tag.hasKey("posX", Constants.NBT.TAG_INT) && tag.hasKey("posY", Constants.NBT.TAG_INT) && tag.hasKey("posZ", Constants.NBT.TAG_INT))
            {
                addBridgeLocation(new BlockPosEU(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"), tag.getInteger("Dim"), 1), false);
            }
        }
    }

    public static NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        int count = bridgeLocations.size();
        if (count > 0)
        {
            NBTTagList tagList = new NBTTagList();
            for (int i = 0; i < count; ++i)
            {
                BlockPosEU pos = bridgeLocations.get(i);
                if (pos != null)
                {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setInteger("Dim", pos.dimension);
                    tag.setInteger("posX", pos.posX);
                    tag.setInteger("posY", pos.posY);
                    tag.setInteger("posZ", pos.posZ);
                    tagList.appendTag(tag);
                }
            }

            if (tagList.tagCount() > 0)
            {
                nbt.setTag("EnergyBridges", tagList);
            }
        }

        return nbt;
    }

    public static void readFromDisk()
    {
        // Clear the data structures when reading the data for a world/save, so that valid Energy Bridges
        // from another world won't carry over to a world/save that doesn't have the file yet.
        bridgeLocations.clear();
        bridgeCounts.clear();

        try
        {
            File saveDir = DimensionManager.getCurrentSaveRootDirectory();
            if (saveDir == null)
            {
                return;
            }

            File file = new File(new File(saveDir, Reference.MOD_ID), "energybridges.dat");
            if (file.exists() == true && file.isFile() == true)
            {
                readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(file)));
            }
        }
        catch (Exception e)
        {
            EnderUtilities.logger.warn("Failed to read Energy Bridge data from file!");
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
            CompressedStreamTools.writeCompressed(writeToNBT(new NBTTagCompound()), new FileOutputStream(fileTmp));

            if (fileReal.exists())
            {
                fileReal.delete();
            }

            fileTmp.renameTo(fileReal);
            dirty = false;
        }
        catch (Exception e)
        {
            EnderUtilities.logger.warn("Failed to write Energy Bridge data to file!");
        }
    }
}
