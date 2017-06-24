package fi.dy.masa.enderutilities;

import org.apache.logging.log4j.Logger;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import fi.dy.masa.enderutilities.config.ConfigReader;
import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import fi.dy.masa.enderutilities.util.datafixer.TileEntityID;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
     guiFactory = "fi.dy.masa.enderutilities.config.EnderUtilitiesGuiFactory",
     updateJSON = "https://raw.githubusercontent.com/maruohon/enderutilities/master/update.json",
     acceptedMinecraftVersions = "1.12",
     dependencies = "required-after:forge@[14.21.0.2359,);")
public class EnderUtilities
{
    public static final int DATA_FIXER_VERSION = 922;

    @Instance(Reference.MOD_ID)
    public static EnderUtilities instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static IProxy proxy;
    public static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ConfigReader.loadConfigsFromFile(event.getSuggestedConfigurationFile());
        ModRegistry.checkLoadedMods();

        proxy.registerEventHandlers();
        proxy.registerEntities();
        proxy.registerKeyBindings();
        proxy.registerRenderers();

        PacketHandler.init(); // Initialize network stuff
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new EnderUtilitiesGUIHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.registerColorHandlers();
    }

    @Mod.EventHandler
    public void onServerAboutToStartEvent(FMLServerAboutToStartEvent event)
    {
        // Register data fixers
        ModFixs dataFixer = proxy.getDataFixer();
        TileEntityID renames = new TileEntityID();
        dataFixer.registerFix(FixTypes.BLOCK_ENTITY, renames);
        dataFixer.registerFix(FixTypes.ITEM_INSTANCE, renames);
    }

    @Mod.EventHandler
    public void onServerStartingEvent(FMLServerStartingEvent event)
    {
        //EnderUtilities.logger.info("Clearing chunk loading timeouts");
        ConfigReader.reLoadAllConfigs(true);
        ChunkLoading.getInstance().init();
        EnergyBridgeTracker.readFromDisk();
        PlacementProperties.getInstance().readFromDisk();
    }
}
