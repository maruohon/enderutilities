package fi.dy.masa.minecraft.mods.enderutilities.reference;

public class Reference
{
	public static final String MOD_ID = "enderutilities";
	public static final String MOD_NAME = "Ender Utilities";
	public static final String MOD_VERSION = "0.1";
	public static final String CLASS_CLIENT_PROXY = "fi.dy.masa.minecraft.mods.enderutilities.proxy.ClientProxy";
	public static final String CLASS_COMMON_PROXY = "fi.dy.masa.minecraft.mods.enderutilities.proxy.ServerProxy";

	public static final String NAME_CONTAINER_ENDER_FURNACE		= "container.enderfurnace";

	public static final String NAME_ITEM_ENDER_ARROW			= "enderarrow";
	public static final String NAME_ITEM_ENDER_BAG				= "enderbag";
	public static final String NAME_ITEM_ENDER_BOW				= "enderbow";
	public static final String NAME_ITEM_ENDER_BUCKET			= "enderbucket";
	public static final String NAME_ITEM_ENDER_FURNACE			= "enderfurnace";
	public static final String NAME_ITEM_ENDER_LASSO			= "enderlasso";
	public static final String NAME_ITEM_ENDER_PEARL_REUSABLE	= "enderpearlreusable";

	public static final String NAME_TILE_ENDER_FURNACE			= "enderfurnace";

	public static final String NAME_ENTITY_ENDER_ARROW			= "enderarrow";
	public static final String NAME_ENTITY_ENDER_PEARL_REUSABLE = "enderpearlreusable";

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
