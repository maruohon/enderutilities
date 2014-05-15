package fi.dy.masa.minecraft.mods.enderutilities;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.minecraft.mods.enderutilities.init.ModItems;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class EnderUtilities
{
	@Instance(Reference.MOD_ID)
	public static EnderUtilities instance;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		// Initialize mod items
		ModItems.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
	}

/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
*/
}
