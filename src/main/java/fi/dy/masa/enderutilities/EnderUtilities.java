package fi.dy.masa.enderutilities;

import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.config.ConfigReader;
import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.registry.ModRegistry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.PlacementProperties;
import fi.dy.masa.enderutilities.util.datafixer.TileEntityID;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
     guiFactory = "fi.dy.masa.enderutilities.config.EnderUtilitiesGuiFactory",
     updateJSON = "https://raw.githubusercontent.com/maruohon/enderutilities/master/update.json",
     acceptedMinecraftVersions = "1.12")
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

        EnderUtilitiesItems.registerItems();
        EnderUtilitiesBlocks.registerBlocks();

        EnderUtilitiesBlocks.registerRecipes();

        proxy.registerModels();
        proxy.registerEntities();
        proxy.registerTileEntities();
        proxy.registerKeyBindings();
        proxy.registerEventHandlers();
        proxy.registerRenderers();
        proxy.registerSounds();

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

    @Mod.EventHandler
    public void onMissingMappingEvent(FMLMissingMappingsEvent event)
    {
        List<MissingMapping> list = event.get();
        Map<String, String> renameMap = TileEntityID.getMap();

        for (MissingMapping mapping : list)
        {
            if (mapping.type == GameRegistry.Type.BLOCK)
            {
                ResourceLocation oldLoc = mapping.resourceLocation;

                if (oldLoc.getResourceDomain().equals(Reference.MOD_ID))
                {
                    String newName = renameMap.get(oldLoc.toString());

                    if (newName != null)
                    {
                        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(newName));

                        if (block != null && block != Blocks.AIR)
                        {
                            mapping.remap(block);
                            logger.info("Re-mapped block '{}' to '{}'", oldLoc, newName);
                        }
                    }
                }
            }
            else if (mapping.type == GameRegistry.Type.ITEM)
            {
                ResourceLocation oldLoc = mapping.resourceLocation;

                if (oldLoc.getResourceDomain().equals(Reference.MOD_ID))
                {
                    String newName = renameMap.get(oldLoc.toString());

                    if (newName != null)
                    {
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(newName));

                        if (item != null && item != Items.AIR)
                        {
                            mapping.remap(item);
                            logger.info("Re-mapped item '{}' to '{}'", oldLoc, newName);
                        }
                    }
                }
            }
        }
    }
}
