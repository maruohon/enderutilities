package fi.dy.masa.enderutilities.event;

import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.datafixer.TileEntityID;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class MissingMappingEventHandler
{
    @SubscribeEvent
    public static void onMissingMappingEventBlocks(RegistryEvent.MissingMappings<Block> event)
    {
        List<Mapping<Block>> list = event.getMappings();
        Map<String, String> renameMap = TileEntityID.getMap();

        for (Mapping<Block> mapping : list)
        {
            ResourceLocation oldLoc = mapping.key;
            String newName = renameMap.get(oldLoc.toString());

            if (newName != null)
            {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(newName));

                if (block != null && block != Blocks.AIR)
                {
                    mapping.remap(block);
                    EnderUtilities.logger.info("Re-mapped block '{}' => '{}'", oldLoc, newName);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMissingMappingEventItems(RegistryEvent.MissingMappings<Item> event)
    {
        List<Mapping<Item>> list = event.getMappings();
        Map<String, String> renameMap = TileEntityID.getMap();

        for (Mapping<Item> mapping : list)
        {
            ResourceLocation oldLoc = mapping.key;
            String newName = renameMap.get(oldLoc.toString());

            if (newName != null)
            {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(newName));

                if (item != null && item != Items.AIR)
                {
                    mapping.remap(item);
                    EnderUtilities.logger.info("Re-mapped item '{}' => '{}'", oldLoc, newName);
                }
            }
        }
    }
}
