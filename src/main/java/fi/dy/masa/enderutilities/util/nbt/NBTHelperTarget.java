package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;

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

    public static NBTHelperTarget getTarget(ItemStack stack)
    {
        if (stack != null)
        {
            NBTHelperTarget target = new NBTHelperTarget();
            if (target.readTargetTagFromNBT(stack.getTagCompound()) != null)
            {
                return target;
            }
        }

        return null;
    }

    public static NBTHelperTarget getTargetFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        return getTarget(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
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

    public static boolean hasTargetTag(ItemStack stack)
    {
        return (stack != null && hasTargetTag(stack.getTagCompound()) == true);
    }

    public static boolean selectedModuleHasTargetTag(ItemStack toolStack, ModuleType moduleType)
    {
        return hasTargetTag(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
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

    public static NBTHelperTarget readTargetFromNBT(NBTTagCompound nbt)
    {
        NBTHelperTarget target = new NBTHelperTarget();
        target.readTargetTagFromNBT(nbt);

        return target;
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

    /* This is for compatibility when upgrading from 0.3.x.
     * It tries to transfer old style target data tags from the containing item to
     * the first link crystal that has no target tag, and then removes the old target tag from the item.
     * FIXME Remove this sometime around 0.5.0 or 0.6.0.
     */
    public static boolean compatibilityTransferTargetData(ItemStack toolStack)
    {
        if (toolStack == null || toolStack.getTagCompound() == null || (toolStack.getItem() instanceof IModular) == false)
        {
            return false;
        }

        IModular item = (IModular)toolStack.getItem();
        // Only handle Ender Bow, Ender Lasso and Ender Porter target data
        if (! (item == EnderUtilitiesItems.enderLasso
            || item == EnderUtilitiesItems.enderBow
            || item == EnderUtilitiesItems.enderPorter))
        {
            return false;
        }

        NBTTagCompound toolNbt = toolStack.getTagCompound();
        if (toolNbt.hasKey("Target", Constants.NBT.TAG_COMPOUND) == false)
        {
            return false;
        }

        NBTTagCompound tag = toolNbt.getCompoundTag("Target");
        // BlockFace tag was INT, otherwise we could have used the current method to check for target tag
        if (! (tag != null &&
            tag.hasKey("posX", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("posY", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("posZ", Constants.NBT.TAG_INT) == true &&
            tag.hasKey("Dim", Constants.NBT.TAG_INT) == true &&
            //tag.hasKey("BlockName", Constants.NBT.TAG_STRING) == true &&
            //tag.hasKey("BlockMeta", Constants.NBT.TAG_BYTE) == true &&
            tag.hasKey("BlockFace", Constants.NBT.TAG_INT) == true))
        {
            return false;
        }

        // See how many link crystals are installed
        if (item.getModuleCount(toolStack, ModuleType.TYPE_LINKCRYSTAL) == 0)
        {
            return false;
        }

        // Read the old target tag
        NBTHelperTarget target = new NBTHelperTarget();
        target.posX = tag.getInteger("posX");
        target.posY = tag.getInteger("posY");
        target.posZ = tag.getInteger("posZ");
        target.dimension = tag.getInteger("Dim");
        target.dimensionName = tag.getString("DimName");
        target.blockName = tag.getString("BlockName");
        target.blockMeta = tag.getByte("BlockMeta");
        target.blockFace = tag.getByte("BlockFace");
        target.forgeDir = ForgeDirection.getOrientation(target.blockFace);

        target.dPosX = tag.hasKey("dPosX", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosX") : target.posX + 0.5d;
        target.dPosY = tag.hasKey("dPosY", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosY") : target.posY;
        target.dPosZ = tag.hasKey("dPosZ", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosZ") : target.posZ + 0.5d;

        if (toolNbt.hasKey("Items", Constants.NBT.TAG_LIST) == false)
        {
            return false;
        }

        NBTTagList nbtTagList = toolNbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        if (nbtTagList == null)
        {
            return false;
        }

        int listNumStacks = nbtTagList.tagCount();

        // Try to find a link crystal that has no target tag yet
        for (int i = 0; i < listNumStacks; ++i)
        {
            NBTTagCompound moduleTag = nbtTagList.getCompoundTagAt(i);
            ItemStack moduleStack = ItemStack.loadItemStackFromNBT(moduleTag);
            if (UtilItemModular.moduleTypeEquals(moduleStack, ModuleType.TYPE_LINKCRYSTAL) == true)
            {
                NBTTagCompound moduleNbt = moduleStack.getTagCompound();
                if (moduleNbt == null || NBTHelperTarget.hasTargetTag(moduleNbt) == false)
                {
                    moduleNbt = target.writeToNBT(moduleNbt);
                    moduleStack.setTagCompound(moduleNbt);
                    // Write the new module ItemStack to the compound tag of the old one, so that we
                    // preserve the Slot tag and any other non-ItemStack tags of the old one.
                    nbtTagList.func_150304_a(i, moduleStack.writeToNBT(moduleTag));
                    toolNbt.removeTag("Target");
                    //System.out.println("post transfering target... lc: " + (i + 1) + " moduleNbt: " + moduleNbt + " toolNbt: " + toolNbt);
                    return true;
                }
            }
        }
        return false;
    }
}
