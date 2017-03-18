package fi.dy.masa.enderutilities.item.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockASU extends ItemBlockStorage
{
    public ItemBlockASU(Block block)
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
