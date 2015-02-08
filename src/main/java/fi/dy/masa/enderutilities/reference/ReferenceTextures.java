package fi.dy.masa.enderutilities.reference;

import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.util.ResourceLocationHelper;

public class ReferenceTextures
{
    public static final String RESOURCE_PREFIX = Reference.MOD_ID.toLowerCase() + ":";
    public static final String GUI_SHEET_LOCATION = "textures/gui/";
    public static final String ITEM_SHEET_LOCATION = "textures/items/";
    public static final String MODEL_TEXTURE_LOCATION = "textures/models/";


    public static ResourceLocation getGuiTexture(String name)
    {
        return ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + name + ".png");
    }

    public static String getItemTextureName(String name)
    {
        return Reference.MOD_ID + ":item." + name;
    }

    public static String getEntityTextureName(String name)
    {
        return Reference.MOD_ID + ":textures/entity/entity." + name + ".png";
    }

    public static String getTileName(String name)
    {
        return Reference.MOD_ID + ":tile." + name;
    }
}
