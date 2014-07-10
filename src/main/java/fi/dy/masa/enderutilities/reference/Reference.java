package fi.dy.masa.enderutilities.reference;

public class Reference
{
	public static final String MOD_ID = "enderutilities";
	public static final String MOD_NAME = "Ender Utilities";
	public static final String MOD_VERSION = "@MODVERSION@";
	public static final String CLASS_CLIENT_PROXY = "fi.dy.masa.enderutilities.proxy.ClientProxy";
	public static final String CLASS_COMMON_PROXY = "fi.dy.masa.enderutilities.proxy.ServerProxy";

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
