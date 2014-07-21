package fi.dy.masa.enderutilities.reference;

import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.util.ResourceLocationHelper;

public class Textures
{
	public static final String RESOURCE_PREFIX = Reference.MOD_ID.toLowerCase() + ":";
	public static final String GUI_SHEET_LOCATION = "textures/gui/";
	public static final String ITEM_SHEET_LOCATION = "textures/items/";
	public static final String MODEL_TEXTURE_LOCATION = "textures/models/";

	// Item textures (for custom rendered items)
	public static final ResourceLocation TEXTURE_RESOURCE_ITEM_ENDER_BUCKET = ResourceLocationHelper.getResourceLocation(ITEM_SHEET_LOCATION + "item.enderbucket.32.sheet.png");

	public static ResourceLocation getGuiTexture(String name)
	{
		return ResourceLocationHelper.getResourceLocation(GUI_SHEET_LOCATION + name + ".png");
	}

	public static String getTextureName(String name)
	{
		return Reference.MOD_ID + ":" + name;
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
