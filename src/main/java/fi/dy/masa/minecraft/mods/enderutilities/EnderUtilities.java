package fi.dy.masa.minecraft.mods.enderutilities;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.minecraft.mods.enderutilities.event.EntityInteract;
import fi.dy.masa.minecraft.mods.enderutilities.init.EnderUtilitiesItems;
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
		EnderUtilitiesItems.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new EntityInteract());
	}

/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
*/
}
