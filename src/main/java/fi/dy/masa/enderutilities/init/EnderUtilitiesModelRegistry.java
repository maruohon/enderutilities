package fi.dy.masa.enderutilities.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.reference.Reference;

public class EnderUtilitiesModelRegistry
{
    public static void registerItemModel(String name, int damage)
    {
        Item item = GameRegistry.findItem(Reference.MOD_ID, name);
        ModelResourceLocation mrl = new ModelResourceLocation(Reference.MOD_ID + ":" + name, "inventory");
        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, damage, mrl);
    }
}
