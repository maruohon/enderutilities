package fi.dy.masa.enderutilities.client.renderer.model;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;

public class ItemMeshDefinitionWrapper implements ItemMeshDefinition
{
    private static ItemMeshDefinitionWrapper instance;

    public ItemMeshDefinitionWrapper()
    {
    }

    public static ItemMeshDefinitionWrapper instance()
    {
        if (instance == null)
        {
            instance = new ItemMeshDefinitionWrapper();
        }

        return instance;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        if (stack.getItem() instanceof ItemEnderUtilities)
        {
            return ((ItemEnderUtilities)stack.getItem()).getModelLocation(stack);
        }

        return null;
    }
}
