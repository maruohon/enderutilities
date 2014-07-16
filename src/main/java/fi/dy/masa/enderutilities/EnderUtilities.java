package fi.dy.masa.enderutilities;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class EnderUtilities
{
	@Instance(Reference.MOD_ID)
	public static EnderUtilities instance;

	@SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
	public static IProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.registerKeyBindings();

		// Initialize network stuff
		PacketHandler.init();

		// Initialize mod items
		EnderUtilitiesItems.init();

		// Initialize mod blocks
		EnderUtilitiesBlocks.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.registerEntities();
		proxy.registerEventHandlers();
		proxy.registerRenderers();
		proxy.registerTileEntities();
	}

/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
*/
}
