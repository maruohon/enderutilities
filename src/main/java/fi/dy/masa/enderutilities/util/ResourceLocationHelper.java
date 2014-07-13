package fi.dy.masa.enderutilities.util;

import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.reference.Reference;

/*
 * Copied from EE3, thanks Pahimar
 */
public class ResourceLocationHelper
{
	public static ResourceLocation getResourceLocation(String modId, String path)
	{
		return new ResourceLocation(modId, path);
	}

	public static ResourceLocation getResourceLocation(String path)
	{
		return getResourceLocation(Reference.MOD_ID.toLowerCase(), path);
	}
}
