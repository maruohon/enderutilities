package fi.dy.masa.enderutilities.inventory.wrapper;

import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipe;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;

public class ItemHandlerCraftResult extends ItemStackHandlerBasic
{
    @Nullable
    private IRecipe recipe;

    public ItemHandlerCraftResult()
    {
        super(1);
    }

    public void setRecipe(@Nullable IRecipe recipe)
    {
        this.recipe = recipe;
    }

    @Nullable
    public IRecipe getRecipe()
    {
        return this.recipe;
    }
}
