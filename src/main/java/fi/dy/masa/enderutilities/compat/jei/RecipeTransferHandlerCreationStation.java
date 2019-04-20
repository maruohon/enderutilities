package fi.dy.masa.enderutilities.compat.jei;

import net.minecraft.entity.player.EntityPlayer;
import fi.dy.masa.enderutilities.inventory.container.ContainerCreationStation;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.startup.StackHelper;
import mezz.jei.transfer.BasicRecipeTransferHandler;

public class RecipeTransferHandlerCreationStation extends BasicRecipeTransferHandler<ContainerCreationStation>
{
    public RecipeTransferHandlerCreationStation(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper,
            IRecipeTransferInfo<ContainerCreationStation> transferHelper)
    {
        super(stackHelper, handlerHelper, transferHelper);
    }

    @Override
    public IRecipeTransferError transferRecipe(ContainerCreationStation container, IRecipeLayout recipeLayout,
            EntityPlayer player, boolean maxTransfer, boolean doTransfer)
    {
        if (container.isInventoryAccessible())
        {
            return super.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
        }

        return null;
    }
}
