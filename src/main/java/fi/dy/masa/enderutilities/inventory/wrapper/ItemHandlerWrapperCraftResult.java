package fi.dy.masa.enderutilities.inventory.wrapper;

import javax.annotation.Nullable;
import net.minecraft.item.crafting.IRecipe;
import fi.dy.masa.enderutilities.inventory.ItemStackHandlerBasic;

public class ItemHandlerWrapperCraftResult extends ItemStackHandlerBasic
{
    @Nullable
    private IRecipe recipe;

    public ItemHandlerWrapperCraftResult()
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
