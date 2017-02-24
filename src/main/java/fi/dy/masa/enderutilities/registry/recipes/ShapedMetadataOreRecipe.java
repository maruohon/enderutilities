package fi.dy.masa.enderutilities.registry.recipes;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedMetadataOreRecipe extends ShapedOreRecipe
{
    private final Item sourceItem;
    private final int mask;

    /**
     * Registers a recipe where the metadata value of the first occurence of <b>sourceItem</b> in the recipe
     * directly determines the metadata of the result ItemStack.
     * @param result
     * @param sourceItem
     * @param recipe
     */
    public ShapedMetadataOreRecipe(ItemStack result, Item sourceItem, Object... recipe)
    {
        this(result, sourceItem, 0, recipe);
    }

    /**
     * Registers a recipe where the metadata value of the first occurence of <b>sourceItem</b> in the recipe
     * bitwise OR'ed into <b>mask</b> determines the final metadata of the result ItemStack.
     * @param result
     * @param sourceItem
     * @param mask
     * @param recipe
     */
    public ShapedMetadataOreRecipe(ItemStack result, Block sourceBlock, int mask, Object... recipe)
    {
        super(result, recipe);

        this.sourceItem = Item.getItemFromBlock(sourceBlock);
        this.mask = mask;
    }

    /**
     * Registers a recipe where the metadata value of the first occurence of <b>sourceItem</b> in the recipe
     * bitwise OR'ed into <b>mask</b> determines the final metadata of the result ItemStack.
     * @param result
     * @param sourceItem
     * @param mask
     * @param recipe
     */
    public ShapedMetadataOreRecipe(ItemStack result, Item sourceItem, int mask, Object... recipe)
    {
        super(result, recipe);

        this.sourceItem = sourceItem;
        this.mask = mask;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack result = super.getCraftingResult(inv);

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack tmp = inv.getStackInSlot(i);

            // Take or merge the NBT from the first item on the crafting grid that matches the set "source" item
            if (tmp != null && tmp.getItem() == this.sourceItem)
            {
                result.setItemDamage(tmp.getMetadata() | this.mask);

                break;
            }
        }

        return result;
    }
}
