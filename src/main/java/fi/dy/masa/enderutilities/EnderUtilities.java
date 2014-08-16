package fi.dy.masa.enderutilities;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.EUConfigReader;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class EnderUtilities
{
	@Instance(Reference.MOD_ID)
	public static EnderUtilities instance;

	@SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
	public static IProxy proxy;
	public static org.apache.logging.log4j.Logger logger;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		instance = this;

		logger = event.getModLog();

		EUConfigReader.loadConfigsAll(event.getModConfigurationDirectory());

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
		proxy.registerFuelHandlers();
		proxy.registerRenderers();
		proxy.registerTileEntities();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new EnderUtilitiesGUIHandler());
	}

/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
*/
}
