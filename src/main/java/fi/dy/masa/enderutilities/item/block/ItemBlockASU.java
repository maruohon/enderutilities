package fi.dy.masa.enderutilities.item.block;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;

public class ItemBlockASU extends ItemBlockStorage
{
    public ItemBlockASU(BlockEnderUtilities block)
    {
        super(block);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        return this.block.getUnlocalizedName();
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = super.getItemStackDisplayName(stack);
        name = name.replace("%s", String.valueOf(stack.getMetadata() + 1));
        return name;
    }
}
