package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;
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
    public BlockPos pos;
    public int dimension;
    public String dimensionName;
    public boolean hasAngle;
    public float yaw;
    public float pitch;
    public String blockName;
    public int blockMeta;
    /* Face of the target block */
    public int blockFace;
    public EnumFacing facing;

    public NBTHelperTarget()
    {
        this.posX = 0;
        this.posY = 0;
        this.posZ = 0;
        this.dPosX = 0.0d;
        this.dPosY = 0.0d;
        this.dPosZ = 0.0d;
        this.pos = new BlockPos(0, 0, 0);
        this.dimension = 0;
        this.dimensionName = "";
        this.hasAngle = false;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.blockName = "";
        this.blockMeta = 0;
        this.blockFace = -1;
        this.facing = EnumFacing.UP;
    }

    public static NBTHelperTarget getTargetFromItem(ItemStack stack)
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
        return getTargetFromItem(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public static boolean nbtHasTargetTag(NBTTagCompound nbt)
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

    public static boolean itemHasTargetTag(ItemStack stack)
    {
        return (stack != null && nbtHasTargetTag(stack.getTagCompound()) == true);
    }

    public static boolean selectedModuleHasTargetTag(ItemStack toolStack, ModuleType moduleType)
    {
        return itemHasTargetTag(UtilItemModular.getSelectedModuleStack(toolStack, moduleType));
    }

    public NBTTagCompound readTargetTagFromNBT(NBTTagCompound nbt)
    {
        if (nbtHasTargetTag(nbt) == false)
        {
            return null;
        }

        NBTTagCompound tag = nbt.getCompoundTag("Target");
        this.posX = tag.getInteger("posX");
        this.posY = tag.getInteger("posY");
        this.posZ = tag.getInteger("posZ");
        this.pos = new BlockPos(this.posX, this.posY, this.posZ);
        this.dimension = tag.getInteger("Dim");
        this.dimensionName = tag.getString("DimName");
        this.blockName = tag.getString("BlockName");
        this.blockMeta = tag.getByte("BlockMeta");
        this.blockFace = tag.getByte("BlockFace");
        this.facing = EnumFacing.getFront(this.blockFace);

        this.dPosX = tag.hasKey("dPosX", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosX") : this.posX + 0.5d;
        this.dPosY = tag.hasKey("dPosY", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosY") : this.posY;
        this.dPosZ = tag.hasKey("dPosZ", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosZ") : this.posZ + 0.5d;

        if (tag.hasKey("Yaw", Constants.NBT.TAG_FLOAT) == true && tag.hasKey("Pitch", Constants.NBT.TAG_FLOAT) == true)
        {
            this.hasAngle = true;
            this.yaw = tag.getFloat("Yaw");
            this.pitch = tag.getFloat("Pitch");
        }

        return tag;
    }

    public static NBTHelperTarget readTargetFromNBT(NBTTagCompound nbt)
    {
        NBTHelperTarget target = new NBTHelperTarget();
        target.readTargetTagFromNBT(nbt);

        return target;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, BlockPos pos, double dx, double dy, double dz, int dim, String dimName, String blockName, int meta, EnumFacing face, float yaw, float pitch, boolean hasAngle)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("posX", pos.getX());
        tag.setInteger("posY", pos.getY());
        tag.setInteger("posZ", pos.getZ());
        tag.setDouble("dPosX", dx);
        tag.setDouble("dPosY", dy);
        tag.setDouble("dPosZ", dz);
        tag.setInteger("Dim", dim);
        tag.setString("DimName", dimName);
        tag.setString("BlockName", blockName);
        tag.setByte("BlockMeta", (byte)meta);
        tag.setByte("BlockFace", (byte)face.getIndex());

        if (hasAngle == true)
        {
            tag.setFloat("Yaw", yaw);
            tag.setFloat("Pitch", pitch);
        }

        nbt.setTag("Target", tag);

        return nbt;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, BlockPos pos, int dim, EnumFacing face, double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
    {
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        double dPosX = pos.getX();
        double dPosY = pos.getY();
        double dPosZ = pos.getZ();

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

                IBlockState blockState = world.getBlockState(pos);
                Block block = blockState.getBlock();
                UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor(block);
                if (ui != null)
                {
                    blockName = ui.toString();
                }
                else
                {
                    blockName = Block.blockRegistry.getNameForObject(block).toString();
                }

                meta = block.getMetaFromState(blockState);
            }
        }

        return writeTargetTagToNBT(nbt, pos, dPosX, dPosY, dPosZ, dim, dimName, blockName, meta, face, yaw, pitch, hasAngle);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeTargetTagToNBT(nbt, this.pos, this.dPosX, this.dPosY, this.dPosZ, this.dimension,
            this.dimensionName, this.blockName, this.blockMeta, this.facing, this.yaw, this.pitch, this.hasAngle);
    }

    public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
    {
        return NBTHelper.writeTagToNBT(nbt, "Target", null);
    }

    public static void removeTargetTagFromItem(ItemStack stack)
    {
        if (stack != null)
        {
            stack.setTagCompound(removeTargetTagFromNBT(stack.getTagCompound()));
        }
    }

    public static boolean removeTargetTagFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            removeTargetTagFromItem(moduleStack);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public static void writeTargetTagToItem(ItemStack stack, BlockPos pos, int dim, EnumFacing face, double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
    {
        if (stack != null)
        {
            stack.setTagCompound(writeTargetTagToNBT(stack.getTagCompound(),pos, dim, face, hitX, hitY, hitZ, doHitOffset, yaw, pitch, hasAngle));
        }
    }

    public static boolean writeTargetTagToSelectedModule(ItemStack toolStack, ModuleType moduleType, BlockPos pos, int dim, EnumFacing face, double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            writeTargetTagToItem(moduleStack, pos, dim, face, hitX, hitY, hitZ, doHitOffset, yaw, pitch, hasAngle);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public boolean isTargetBlockUnchanged()
    {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null)
        {
            return false;
        }

        World world = server.worldServerForDimension(this.dimension);
        if (world == null)
        {
            return false;
        }

        IBlockState blockState = world.getBlockState(new BlockPos(this.posX, this.posY, this.posZ));
        Block block = blockState.getBlock();

        // The target block unique name and metadata matches what we have stored
        if (this.blockName != null && this.blockName.equals(Block.blockRegistry.getNameForObject(block).toString()) == true
            && this.blockMeta == block.getMetaFromState(blockState))
        {
            return true;
        }

        return false;
    }

    /**
     * This is for compatibility when upgrading from 0.3.x.
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
        target.pos = new BlockPos(target.posX, target.posY, target.posZ);
        target.dimension = tag.getInteger("Dim");
        target.dimensionName = tag.getString("DimName");
        target.blockName = tag.getString("BlockName");
        target.blockMeta = tag.getByte("BlockMeta");
        target.blockFace = tag.getByte("BlockFace");
        target.facing = EnumFacing.getFront(target.blockFace);

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
                if (moduleNbt == null || NBTHelperTarget.nbtHasTargetTag(moduleNbt) == false)
                {
                    moduleNbt = target.writeToNBT(moduleNbt);
                    moduleStack.setTagCompound(moduleNbt);
                    // Write the new module ItemStack to the compound tag of the old one, so that we
                    // preserve the Slot tag and any other non-ItemStack tags of the old one.
                    nbtTagList.set(i, moduleStack.writeToNBT(moduleTag));
                    toolNbt.removeTag("Target");
                    //System.out.println("post transfering target... lc: " + (i + 1) + " moduleNbt: " + moduleNbt + " toolNbt: " + toolNbt);
                    return true;
                }
            }
        }
        return false;
    }
}
