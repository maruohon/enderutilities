package fi.dy.masa.enderutilities.util.nbt;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.util.EntityUtils;

public class TargetData
{
    public BlockPos pos;
    public double dPosX;
    public double dPosY;
    public double dPosZ;
    public int dimension;
    public String dimensionName;
    public boolean hasRotation;
    public float yaw;
    public float pitch;
    public String blockName;
    public int blockMeta;
    public int itemMeta;
    public int blockFace;
    public EnumFacing facing;

    public TargetData()
    {
        this.pos = new BlockPos(0, 0, 0);
        this.dPosX = 0.0d;
        this.dPosY = 0.0d;
        this.dPosZ = 0.0d;
        this.dimension = 0;
        this.dimensionName = "";
        this.hasRotation = false;
        this.yaw = 0.0f;
        this.pitch = 0.0f;
        this.blockName = "";
        this.blockMeta = 0;
        this.itemMeta = 0;
        this.blockFace = -1;
        this.facing = EnumFacing.UP;
    }

    public static TargetData getTargetFromItem(ItemStack stack)
    {
        if (stack != null)
        {
            TargetData target = new TargetData();
            if (target.readTargetTagFromNBT(stack.getTagCompound()) != null)
            {
                return target;
            }
        }

        return null;
    }

    public static TargetData getTargetFromSelectedModule(ItemStack toolStack, ModuleType moduleType)
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
        this.pos = new BlockPos(tag.getInteger("posX"), tag.getInteger("posY"), tag.getInteger("posZ"));
        this.dimension = tag.getInteger("Dim");
        this.dimensionName = tag.getString("DimName");
        this.blockName = tag.getString("BlockName");
        this.blockMeta = tag.getByte("BlockMeta");
        this.itemMeta = tag.getByte("ItemMeta");
        this.blockFace = tag.getByte("BlockFace");
        this.facing = EnumFacing.getFront(this.blockFace);

        this.dPosX = tag.hasKey("dPosX", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosX") : this.pos.getX() + 0.5d;
        this.dPosY = tag.hasKey("dPosY", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosY") : this.pos.getY();
        this.dPosZ = tag.hasKey("dPosZ", Constants.NBT.TAG_DOUBLE) == true ? tag.getDouble("dPosZ") : this.pos.getZ() + 0.5d;

        if (tag.hasKey("Yaw", Constants.NBT.TAG_FLOAT) == true && tag.hasKey("Pitch", Constants.NBT.TAG_FLOAT) == true)
        {
            this.hasRotation = true;
            this.yaw = tag.getFloat("Yaw");
            this.pitch = tag.getFloat("Pitch");
        }

        return tag;
    }

    public static TargetData readTargetFromNBT(NBTTagCompound nbt)
    {
        TargetData target = new TargetData();
        target.readTargetTagFromNBT(nbt);

        return target;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, BlockPos pos, double dx, double dy, double dz, int dim,
            String dimName, String blockName, int blockMeta, int itemMeta, EnumFacing side, float yaw, float pitch, boolean hasAngle)
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
        tag.setByte("BlockMeta", (byte)blockMeta);
        tag.setShort("ItemMeta", (short)itemMeta);
        tag.setByte("BlockFace", (byte)side.getIndex());

        if (hasAngle == true)
        {
            tag.setFloat("Yaw", yaw);
            tag.setFloat("Pitch", pitch);
        }

        nbt.setTag("Target", tag);

        return nbt;
    }

    public static NBTTagCompound writeTargetTagToNBT(NBTTagCompound nbt, BlockPos pos, int dim, EnumFacing side, EntityPlayer player,
            double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
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
        int blockMeta = 0;
        int itemMeta = 0;

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null)
        {
            WorldServer world = server.worldServerForDimension(dim);
            if (world != null && world.provider != null)
            {
                dimName = world.provider.getDimensionType().getName();

                IBlockState iBlockState = world.getBlockState(pos);
                Block block = iBlockState.getBlock();
                blockMeta = block.getMetaFromState(iBlockState);

                ItemStack stack = block.getPickBlock(iBlockState, EntityUtils.getRayTraceFromPlayer(world, player, false), world, pos, player);
                if (stack != null)
                {
                    itemMeta = stack.getMetadata();
                }

                ResourceLocation rl = Block.REGISTRY.getNameForObject(block);
                if (rl != null)
                {
                    blockName = rl.toString();
                }
            }
        }

        return writeTargetTagToNBT(nbt, pos, dPosX, dPosY, dPosZ, dim, dimName, blockName, blockMeta, itemMeta, side, yaw, pitch, hasAngle);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        return writeTargetTagToNBT(nbt, this.pos, this.dPosX, this.dPosY, this.dPosZ, this.dimension,
            this.dimensionName, this.blockName, this.blockMeta, this.itemMeta, this.facing, this.yaw, this.pitch, this.hasRotation);
    }

    public static NBTTagCompound removeTargetTagFromNBT(NBTTagCompound nbt)
    {
        return NBTUtils.writeTagToNBT(nbt, "Target", null);
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

    public static void writeTargetTagToItem(ItemStack stack, BlockPos pos, int dim, EnumFacing side, EntityPlayer player,
            double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
    {
        if (stack != null)
        {
            stack.setTagCompound(writeTargetTagToNBT(stack.getTagCompound(), pos, dim, side, player, hitX, hitY, hitZ, doHitOffset, yaw, pitch, hasAngle));
        }
    }

    public static boolean writeTargetTagToSelectedModule(ItemStack toolStack, ModuleType moduleType, BlockPos pos, int dim, EnumFacing side,
            EntityPlayer player, double hitX, double hitY, double hitZ, boolean doHitOffset, float yaw, float pitch, boolean hasAngle)
    {
        ItemStack moduleStack = UtilItemModular.getSelectedModuleStack(toolStack, moduleType);
        if (moduleStack != null)
        {
            writeTargetTagToItem(moduleStack, pos, dim, side, player, hitX, hitY, hitZ, doHitOffset, yaw, pitch, hasAngle);
            UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);

            return true;
        }

        return false;
    }

    public boolean isTargetBlockUnchanged()
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null)
        {
            return false;
        }

        World world = server.worldServerForDimension(this.dimension);
        if (world == null)
        {
            return false;
        }

        IBlockState iBlockState = world.getBlockState(this.pos);
        Block block = iBlockState.getBlock();
        int meta = block.getMetaFromState(iBlockState);

        // The target block unique name and metadata matches what we have stored
        if (this.blockMeta == meta && this.blockName.equals(Block.REGISTRY.getNameForObject(block).toString()) == true)
        {
            return true;
        }

        return false;
    }

    public String getTargetBlockDisplayName()
    {
        Block block = Block.getBlockFromName(this.blockName);
        ItemStack targetStack = new ItemStack(block, 1, this.itemMeta);
        if (targetStack != null && targetStack.getItem() != null)
        {
            return targetStack.getDisplayName();
        }

        return null;
    }

    public String getDimensionName(boolean useFallback)
    {
        try
        {
            DimensionType type = DimensionType.getById(this.dimension);
            return type.getName();
        }
        catch (IllegalArgumentException e)
        {
            EnderUtilities.logger.debug("Failed to get DimensionType by id (" + this.dimension + ")");
        }

        return useFallback == true ? "DIM: " + this.dimension : "";
    }
}
