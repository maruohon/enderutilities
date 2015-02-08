package fi.dy.masa.enderutilities.reference;

import net.minecraft.item.Item;
import net.minecraftforge.common.util.EnumHelper;

public class ReferenceMaterial
{
    public static final String NAME_MATERIAL_ENDERALLOY_ADVANCED = Reference.MOD_ID + ".enderalloyadvanced";

    public static final class Tool
    {
        // name, harvestLevel, maxUses, efficiency, damage, enchantability
        public static final Item.ToolMaterial ENDER_ALLOY_ADVANCED = EnumHelper.addToolMaterial(NAME_MATERIAL_ENDERALLOY_ADVANCED, 3, 2048, 10f, 4f, 13);
    }
}
