package fi.dy.masa.enderutilities.compat.jei;

import net.minecraft.item.ItemStack;

import fi.dy.masa.enderutilities.compat.jei.crafting.RecipeHandlerCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiCreationStation;
import fi.dy.masa.enderutilities.gui.client.GuiEnderFurnace;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;

@mezz.jei.api.JEIPlugin
public class EnderUtilitiesJeiPlugin implements IModPlugin
{
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

        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new RecipeHandlerCreationStation());

        // Creation Station
        registry.addRecipeCategoryCraftingItem(new ItemStack(EnderUtilitiesBlocks.blockMachine_1, 1, 2),
                VanillaRecipeCategoryUid.CRAFTING, VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);

        // Ender Furnace
        registry.addRecipeCategoryCraftingItem(new ItemStack(EnderUtilitiesBlocks.blockMachine_0, 1, 0),
                VanillaRecipeCategoryUid.SMELTING, VanillaRecipeCategoryUid.FUEL);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
    }
}
