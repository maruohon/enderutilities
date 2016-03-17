package fi.dy.masa.enderutilities.setup;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import fi.dy.masa.enderutilities.block.BlockEnderFurnace;
import fi.dy.masa.enderutilities.block.BlockEnergyBridge;
import fi.dy.masa.enderutilities.block.BlockMachine;
import fi.dy.masa.enderutilities.block.BlockStorage;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class EnderUtilitiesBlocks
{
    public static final BlockEnderUtilities blockMachine_0 = new BlockEnderFurnace(ReferenceNames.NAME_TILE_MACHINE_0, 8.0f, 1, Material.iron);
    public static final BlockEnderUtilities blockMachine_1 = new BlockMachine(ReferenceNames.NAME_TILE_MACHINE_1, 8.0f, 1, Material.iron);
    public static final BlockEnderUtilities blockEnergyBridge = new BlockEnergyBridge(ReferenceNames.NAME_TILE_ENERGY_BRIDGE, 8.0f, 2, Material.iron);
    public static final BlockEnderUtilities blockStorage_0 = new BlockStorage(ReferenceNames.NAME_TILE_STORAGE_0, 10.0f, 1, Material.iron);

    public static void init()
    {
        // Register blocks
        if (Configs.disableBlockMachine_0.getBoolean(false) == false) { GameRegistry.registerBlock(blockMachine_0, ItemBlockEnderUtilities.class, ReferenceNames.NAME_TILE_MACHINE_0); }
        if (Configs.disableBlockMachine_1.getBoolean(false) == false) { GameRegistry.registerBlock(blockMachine_1, ItemBlockEnderUtilities.class, ReferenceNames.NAME_TILE_MACHINE_1); }
        if (Configs.disableBlockEnergyBridge.getBoolean(false) == false) { GameRegistry.registerBlock(blockEnergyBridge, ItemBlockEnderUtilities.class, ReferenceNames.NAME_TILE_ENERGY_BRIDGE); }
        if (Configs.disableBlockStorage_0.getBoolean(false) == false) { GameRegistry.registerBlock(blockStorage_0, ItemBlockEnderUtilities.class, ReferenceNames.NAME_TILE_STORAGE_0); }

        ItemStack chest = new ItemStack(Blocks.chest);
        ItemStack craftingtable = new ItemStack(Blocks.crafting_table);
        ItemStack enderChest = new ItemStack(Blocks.ender_chest);
        ItemStack furnace = new ItemStack(Blocks.furnace);
        ItemStack hopper = new ItemStack(Blocks.hopper);
        ItemStack obsidian = new ItemStack(Blocks.obsidian);
        ItemStack piston = new ItemStack(Blocks.piston);
        ItemStack repeater = new ItemStack(Items.repeater);

        ItemStack alloy0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 0);
        ItemStack alloy1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 1);
        ItemStack alloy2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 2);
        ItemStack core0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 10);
        //ItemStack core1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 11);
        //ItemStack core2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 12);
        //ItemStack active_core0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 15);
        ItemStack active_core1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 16);
        ItemStack active_core2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 17);
        //ItemStack ender_stick = new ItemStack(EnderUtilitiesItems.enderPart, 1, 20);
        //ItemStack rope = new ItemStack(EnderUtilitiesItems.enderPart, 1, 21);

        // Register block recipes
        if (Configs.disableRecipeEnderFurnace.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockMachine_0, 1, 0), "OAO", "AFA", "OCO", 'O', obsidian, 'A', alloy1, 'F', furnace, 'C', core0);
        }
        if (Configs.disableRecipeEnderInfuser.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockMachine_1, 1, 0), "AHA", "APA", "OFO", 'A', alloy0, 'H', hopper, 'P', piston, 'O', obsidian, 'F', furnace);
        }
        if (Configs.disableRecipeToolWorkstation.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine_1, 1, 1), "ASA", "ACA", "OHO", 'A', alloy0, 'S', "slimeball", 'C', craftingtable, 'O', obsidian, 'H', chest));
        }
        if (Configs.disableRecipeCreationStation.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine_1, 1, 2), "FRF", "ACA", "OAO", 'F', new ItemStack(blockMachine_0, 1, 0), 'R', craftingtable, 'A', alloy2, 'C', active_core1, 'O', obsidian));
        }

        if (Configs.disableRecipeEnergyBridgeResonator.getBoolean(false) == false && Configs.disableBlockEnergyBridge.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 0), "AGA", "GCG", "AAA", 'A', alloy1, 'G', "blockGlass", 'C', active_core1));
        }
        if (Configs.disableRecipeEnergyBridgeReceiver.getBoolean(false) == false && Configs.disableBlockEnergyBridge.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 1), "AGA", "GCG", "AGA", 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeTransmitter.getBoolean(false) == false && Configs.disableBlockEnergyBridge.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 2), "ASA", "ACA", "AGA", 'S', Items.nether_star, 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }

        if (Configs.disableRecipeMemoryChest_0.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 0), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy0, 'C', chest);
        }
        if (Configs.disableRecipeMemoryChest_1.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 1), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy1, 'C', chest);
        }
        if (Configs.disableRecipeMemoryChest_2.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 2), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy2, 'C', chest);
        }

        if (Configs.disableRecipeHandyChest_0.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 3), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy0, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest_1.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 4), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy1, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest_2.getBoolean(false) == false && Configs.disableBlockStorage_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 5), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy2, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
    }
}
