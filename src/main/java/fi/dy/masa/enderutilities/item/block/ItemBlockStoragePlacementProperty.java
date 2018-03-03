package fi.dy.masa.enderutilities.item.block;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemBlockStoragePlacementProperty extends ItemBlockPlacementProperty
{
    public ItemBlockStoragePlacementProperty(BlockEnderUtilities block)
    {
        super(block);
    }

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
        return ItemBlockStorage.getNBTShareTagImpl(stack);
    }
}
