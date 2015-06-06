package fi.dy.masa.enderutilities;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.ConfigReader;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class EnderUtilities
{
    @Instance(Reference.MOD_ID)
    public static EnderUtilities instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static IProxy proxy;
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        instance = this;
        logger = event.getModLog();
        ConfigReader.loadConfigsAll(event.getSuggestedConfigurationFile());
        proxy.registerKeyBindings();
        PacketHandler.init(); // Initialize network stuff
        EnderUtilitiesItems.init(); // Initialize and register mod items and item recipes
        EnderUtilitiesBlocks.init(); // Initialize and register mod blocks and block recipes
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerEntities();
        proxy.registerEventHandlers();
        proxy.registerRenderers();
        proxy.registerTileEntities();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new EnderUtilitiesGUIHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Registry.registerEnderbagLists();
        Registry.registerTeleportBlacklist();
    }

    @EventHandler
    public void onServerStartingEvent(FMLServerStartingEvent event)
    {
        //EnderUtilities.logger.info("Clearing chunk loading timeouts");
        ChunkLoading.getInstance().init();
        EnergyBridgeTracker.readFromDisk();
    }
}
