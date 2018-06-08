package fi.dy.masa.enderutilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import fi.dy.masa.enderutilities.capabilities.EnderUtilitiesCapabilities;
import fi.dy.masa.enderutilities.config.ConfigReader;
import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.CommonProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import fi.dy.masa.enderutilities.util.datafixer.TileEntityID;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, certificateFingerprint = Reference.FINGERPRINT,
     guiFactory = "fi.dy.masa.enderutilities.config.EnderUtilitiesGuiFactory",
     updateJSON = "https://raw.githubusercontent.com/maruohon/enderutilities/master/update.json",
     acceptedMinecraftVersions = "[1.12.2]",
     dependencies = "required-after:forge@[14.23.3.2694,);") // Currently depends on the new TileEntity registration method with a RL
public class EnderUtilities
{
    public static final int DATA_FIXER_VERSION = 922;

    @Instance(Reference.MOD_ID)
    public static EnderUtilities instance;

    @SidedProxy(clientSide = Reference.PROXY_CLASS_CLIENT, serverSide = Reference.PROXY_CLASS_SERVER)
    public static CommonProxy proxy;

    public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        ConfigReader.loadConfigsFromFile(event.getSuggestedConfigurationFile());
        ModRegistry.checkLoadedMods();

        proxy.registerEventHandlers();
        proxy.registerEntities();
        proxy.registerKeyBindings();
        proxy.registerRenderers();

        EnderUtilitiesCapabilities.register();
        PacketHandler.init(); // Initialize network stuff
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new EnderUtilitiesGUIHandler());
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

    @Mod.EventHandler
    public void onFingerPrintViolation(FMLFingerprintViolationEvent event)
    {
        // Not running in a dev environment
        if (event.isDirectory() == false)
        {
            logger.warn("*********************************************************************************************");
            logger.warn("*****                                    WARNING                                        *****");
            logger.warn("*****                                                                                   *****");
            logger.warn("*****   The signature of the mod file '{}' does not match the expected fingerprint!     *****", event.getSource().getName());
            logger.warn("*****   This might mean that the mod file has been tampered with!                       *****");
            logger.warn("*****   If you did not download the mod {} directly from Curse/CurseForge,       *****", Reference.MOD_NAME);
            logger.warn("*****   or using one of the well known launchers, and you did not                       *****");
            logger.warn("*****   modify the mod file at all yourself, then it's possible,                        *****");
            logger.warn("*****   that it may contain malware or other unwanted things!                           *****");
            logger.warn("*********************************************************************************************");
        }
    }
}
