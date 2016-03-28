package fi.dy.masa.enderutilities.compat.jei;

import fi.dy.masa.enderutilities.compat.jei.crafting.RecipeHandlerCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

@mezz.jei.api.JEIPlugin
public class EnderUtilitiesJeiPlugin implements IModPlugin
{
    @Override
    public void onJeiHelpersAvailable(IJeiHelpers jeiHelpers)
    {
    }

    @Override
    public void onItemRegistryAvailable(IItemRegistry itemRegistry)
    {
    }

    @Override
    public void register(IModRegistry registry)
    {
        registry.addRecipeClickArea(GuiEnderFurnace.class,     58, 35, 22, 15, VanillaRecipeCategoryUid.SMELTING);
        // FIXME Add the custom fuels to a custom handler(?)
        registry.addRecipeClickArea(GuiEnderFurnace.class,     34, 36, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class,  27, 11, 10, 10, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GuiCreationStation.class,   9, 29, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class, 203, 11, 10, 10, VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeClickArea(GuiCreationStation.class, 217, 29, 15, 14, VanillaRecipeCategoryUid.FUEL);

        registry.addRecipeClickArea(GuiCreationStation.class,  97, 36, 10, 10, VanillaRecipeCategoryUid.CRAFTING);
        registry.addRecipeClickArea(GuiCreationStation.class, 133, 72, 10, 10, VanillaRecipeCategoryUid.CRAFTING);

        IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
        recipeTransferRegistry.addRecipeTransferHandler(new RecipeHandlerCreationStation());
    }

    @Override
    public void onRecipeRegistryAvailable(IRecipeRegistry recipeRegistry)
    {
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
