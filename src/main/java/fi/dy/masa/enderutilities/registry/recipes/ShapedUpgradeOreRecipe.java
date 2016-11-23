package fi.dy.masa.enderutilities.registry.recipes;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ShapedUpgradeOreRecipe extends ShapedOreRecipe
{
    /** The first occurence of this item in the crafting grid will be used to copy over NBT data. */
    protected final Item sourceItem;
    protected final int sourceMeta;

    /**
     * The first occurence of the item <b>sourceItem</b> with metadata <b>sourceMeta</b>
     * (or any metadata value if sourceMeta == OreDictionary.WILDCARD_VALUE)
     * in the crafting grid will be used to copy over NBT data from the sourceItem to the crafting result.
     * @param result
     * @param sourceItem
     * @param sourceMeta
     * @param recipe
     */
    public ShapedUpgradeOreRecipe(Block result, Item sourceItem, int sourceMeta, Object... recipe)
    {
        super(result, recipe);
        this.sourceItem = sourceItem;
        this.sourceMeta = sourceMeta;
    }

    /**
     * The first occurence of the item <b>sourceItem</b> with metadata <b>sourceMeta</b>
     * (or any metadata value if sourceMeta == OreDictionary.WILDCARD_VALUE)
     * in the crafting grid will be used to copy over NBT data from the sourceItem to the crafting result.
     * @param result
     * @param sourceItem
     * @param sourceMeta
     * @param recipe
     */
    public ShapedUpgradeOreRecipe(Item result, Item sourceItem, int sourceMeta, Object... recipe)
    {
        super(result, recipe);
        this.sourceItem = sourceItem;
        this.sourceMeta = sourceMeta;
    }

    /**
     * The first occurence of the item <b>sourceItem</b> with metadata <b>sourceMeta</b>
     * (or any metadata value if sourceMeta == OreDictionary.WILDCARD_VALUE)
     * in the crafting grid will be used to copy over NBT data from the sourceItem to the crafting result.
     * @param result
     * @param sourceItem
     * @param sourceMeta
     * @param recipe
     */
    public ShapedUpgradeOreRecipe(ItemStack result, Item sourceItem, int sourceMeta, Object... recipe)
    {
        super(result, recipe);
        this.sourceItem = sourceItem;
        this.sourceMeta = sourceMeta;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv)
    {
        ItemStack result = super.getCraftingResult(inv);

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            ItemStack tmp = inv.getStackInSlot(i);

            // Take or merge the NBT from the first item on the crafting grid that matches the set "source" item
            if (tmp != null && tmp.getItem() == this.sourceItem && tmp.hasTagCompound() &&
                (this.sourceMeta == OreDictionary.WILDCARD_VALUE || tmp.getMetadata() == this.sourceMeta))
            {
                if (result.hasTagCompound())
                {
                    NBTTagCompound tag = tmp.getTagCompound().copy();
                    tag.merge(result.getTagCompound());
                    result.setTagCompound(tag);
                }
                else
                {
                    result.setTagCompound(tmp.getTagCompound().copy());
                }

                break;
            }
        }

        return result;
    }
}
