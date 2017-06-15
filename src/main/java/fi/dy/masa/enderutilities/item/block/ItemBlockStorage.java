package fi.dy.masa.enderutilities.item.block;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemBlockStorage extends ItemBlockEnderUtilities
{
    public ItemBlockStorage(BlockEnderUtilities block)
    {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        NBTUtils.getCachedInventoryStrings(stack, list, 9);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return NBTUtils.getItemStackDisplayName(stack, super.getItemStackDisplayName(stack));
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt != null && nbt.hasKey("InvCache", Constants.NBT.TAG_COMPOUND))
        {
            NBTTagCompound shareNBT = new NBTTagCompound();
            shareNBT.setTag("InvCache", nbt.getCompoundTag("InvCache").copy());
            return shareNBT;
        }

        return nbt;
    }
}
