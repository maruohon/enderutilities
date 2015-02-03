package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class NBTHelperTarget
{
    public int posX;
    public int posY;
    public int posZ;
    public double dPosX;
    public double dPosY;
    public double dPosZ;
    public int dimension;
    public String dimensionName;
    public String blockName;
    public int blockMeta;
    /* Face of the target block */
    public int blockFace;
    public ForgeDirection forgeDir;

    public NBTHelperTarget()
    {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.dPosX = 0.0d;
        this.dPosY = 0.0d;
        this.dPosZ = 0.0d;
        this.dimension = 0;
        this.dimensionName = "";
        this.blockName = "";
        this.blockMeta = 0;
        this.blockFace = -1;
        this.forgeDir = ForgeDirection.UP;
    }

    public static boolean hasTargetTag(NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("Target", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Target");
        if (tag != null &&
            tag.hasKey("posX", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("posY", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("posZ", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("Dim", Constants.NBT.TAG_INT) == true &&
            //tag.hasKey("BlockName", Constants.NBT.TAG_STRING) == true &&
            //tag.hasKey("BlockMeta", Constants.NBT.TAG_BYTE) == true &&
            tag.hasKey("BlockFace", Constants.NBT.TAG_BYTE) == true)
        {
            return true;
        }

        return false;
    }

    public NBTTagCompound readTargetTagFromNBT(NBTTagCompound nbt)
    {
        if (hasTargetTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Target");
        this.posX = tag.getInteger("posX");
        this.posY = tag.getInteger("posY");
        this.posZ = tag.getInteger("posZ");
        this.dimension = tag.getInteger("Dim");
        this.dimensionName = tag.getString("DimName");
        this.blockName = tag.getString("BlockName");
        this.blockMeta = tag.getByte("BlockMeta");
        this.blockFace = tag.getByte("BlockFace");
        this.forgeDir = ForgeDirection.getOrientation(this.blockFace);

        this.dPosX = tag.hasKey("dPosX", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosX") : this.posX + 0.5d;
        this.dPosY = tag.hasKey("dPosY", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosY") : this.posY;
        this.dPosZ = tag.hasKey("dPosZ", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosZ") : this.posZ + 0.5d;

        return tag;
    }

    public static NBTTagCompound writeToNBT(NBTTagCompound nbt, int x, int y, int z, double dx, double dy, double dz, int dim, String dimName, String blockName, int meta, int blockFace)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("posX", x);
        tag.setInteger("posY", y);
        tag.setInteger("posZ", z);
        tag.setDouble("dPosX", dx);
        tag.setDouble("dPosY", dy);
        tag.setDouble("dPosZ", dz);
        tag.setInteger("Dim", dim);
        tag.setString("DimName", dimName);
        tag.setString("BlockName", blockName);
        tag.setByte("BlockMeta", (byte)meta);
        tag.setByte("BlockFace", (byte)blockFace);

        nbt.setTag("Target", tag);

        return nbt;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, int x, int y, int z, int dim, int blockFace, double hitX, double hitY, double hitZ, boolean doHitOffset)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        double dPosX = x;
        double dPosY = y;
        double dPosZ = z;

        if (doHitOffset == true)
        {
            dPosX += hitX;
            dPosY += hitY;
            dPosZ += hitZ;
        }

        String dimName = "";
        String blockName = "";
        int meta = 0;

        if (MinecraftServer.getServer() != null)
        {
            WorldServer world = MinecraftServer.getServer().worldServerForDimension(dim);
            if (world != null && world.provider != null)
            {
                dimName = world.provider.getDimensionName();

                UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(world.getBlock(x, y, z));
                if (ui != null)
                {
                    blockName = ui.toString();
                }
                else
                {
                    blockName = Block.blockRegistry.getNameForObject(world.getBlock(x, y, z));
                }

                meta = world.getBlockMetadata(x, y, z);
            }
        }

        return writeToNBT(nbt, x, y, z, dPosX, dPosY, dPosZ, dim, dimName, blockName, meta, blockFace);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeToNBT(nbt, this.posX, this.posY, this.posZ, this.dPosX, this.dPosY, this.dPosZ, this.dimension,
            this.dimensionName, this.blockName, this.blockMeta, this.blockFace);
    }

    public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
    {
        return NBTHelper.writeTagToNBT(nbt, "Target", null);
    }
}
