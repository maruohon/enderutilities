package fi.dy.masa.enderutilities;

import java.util.List;

import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import fi.dy.masa.enderutilities.gui.EnderUtilitiesGUIHandler;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.proxy.IProxy;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.setup.ConfigReader;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.ModRegistry;
import fi.dy.masa.enderutilities.setup.Registry;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;


@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION,
     guiFactory = "fi.dy.masa.enderutilities.setup.EnderUtilitiesGuiFactory",
     acceptedMinecraftVersions = "1.9")
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

        EnderUtilitiesItems.init(); // Initialize and register mod items and item recipes
        EnderUtilitiesBlocks.init(); // Initialize and register mod blocks and block recipes
        PacketHandler.init(); // Initialize network stuff

        proxy.registerModels();
        proxy.registerEntities();
        proxy.registerTileEntities();
        proxy.registerKeyBindings();
        proxy.registerEventHandlers();
        proxy.registerRenderers();
        proxy.registerSounds();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new EnderUtilitiesGUIHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Registry.registerEnderbagLists();
        Registry.registerTeleportBlacklist();
        ModRegistry.checkLoadedMods();
    }

    @EventHandler
    public void onServerStartingEvent(FMLServerStartingEvent event)
    {
        //EnderUtilities.logger.info("Clearing chunk loading timeouts");
        ChunkLoading.getInstance().init();
        EnergyBridgeTracker.readFromDisk();
    }

    @EventHandler
    public void onMissingMappingEvent(FMLMissingMappingsEvent event)
    {
        List<MissingMapping> list = event.get();

        for (MissingMapping mapping : list)
        {
            if (mapping.type == GameRegistry.Type.BLOCK)
            {
                ResourceLocation oldLoc = mapping.resourceLocation;
                ResourceLocation newLoc = new ResourceLocation(oldLoc.getResourceDomain(), oldLoc.getResourcePath().replaceAll("\\.", "_"));
                EnderUtilities.logger.info(String.format("Re-mapping block '%s' to '%s'", oldLoc, newLoc));
                Block block = Block.blockRegistry.getObject(newLoc);

                if (block != null)
                {
                    mapping.remap(block);
                }
            }
            else if (mapping.type == GameRegistry.Type.ITEM)
            {
                ResourceLocation oldLoc = mapping.resourceLocation;
                ResourceLocation newLoc = new ResourceLocation(oldLoc.getResourceDomain(), oldLoc.getResourcePath().replaceAll("\\.", "_"));
                EnderUtilities.logger.info(String.format("Re-mapping item '%s' to '%s'", oldLoc, newLoc));
                Item item = Item.itemRegistry.getObject(newLoc);

                if (item != null)
                {
                    mapping.remap(item);
                }
            }
        }
    }
}
