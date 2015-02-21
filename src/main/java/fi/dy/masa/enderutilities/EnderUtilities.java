package fi.dy.masa.enderutilities;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import org.apache.logging.log4j.Logger;

import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.ConfigReader;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;


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
        ConfigReader.loadConfigsAll(event.getModConfigurationDirectory());
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
        EnderUtilities.logger.info("Clearing chunk loading timeouts");
        ChunkLoading.getInstance().init();
    }
}
