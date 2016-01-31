package fi.dy.masa.enderutilities.setup;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesTileEntity;
import fi.dy.masa.enderutilities.block.ItemBlockMachine;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class EnderUtilitiesBlocks
{
    public static final Block machine_0 = new BlockEnderUtilitiesInventory(0, ReferenceNames.NAME_TILE_MACHINE_0, 1.0f);
    public static final Block machine_1 = new BlockEnderUtilitiesTileEntity(1, ReferenceNames.NAME_TILE_MACHINE_1, 1.0f);
    public static final Block storage_0 = new BlockEnderUtilitiesInventory(2, ReferenceNames.NAME_TILE_STORAGE_0, 6.0f);

    public static void init()
    {
        // Register blocks
        if (Configs.disableBlockMachine_0.getBoolean(false) == false) { GameRegistry.registerBlock(machine_0, ItemBlockMachine.class, ReferenceNames.NAME_TILE_MACHINE_0); }
        if (Configs.disableBlockMachine_1.getBoolean(false) == false) { GameRegistry.registerBlock(machine_1, ItemBlockMachine.class, ReferenceNames.NAME_TILE_MACHINE_1); }
        if (Configs.disableBlockStorage0.getBoolean(false) == false) { GameRegistry.registerBlock(storage_0, ItemBlockMachine.class, ReferenceNames.NAME_TILE_STORAGE_0); }

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
            GameRegistry.addRecipe(new ItemStack(machine_0, 1, 0), "OAO", "AFA", "OCO", 'O', obsidian, 'A', alloy1, 'F', furnace, 'C', core0);
        }
        if (Configs.disableRecipeEnderInfuser.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(machine_0, 1, 2), "AHA", "APA", "OFO", 'A', alloy0, 'H', hopper, 'P', piston, 'O', obsidian, 'F', furnace);
        }
        if (Configs.disableRecipeToolWorkstation.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_0, 1, 1), "ASA", "ACA", "OHO", 'A', alloy0, 'S', "slimeball", 'C', craftingtable, 'O', obsidian, 'H', chest));
        }
        if (Configs.disableRecipeCreationStation.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_0, 1, 3), "FRF", "ACA", "OAO", 'F', new ItemStack(machine_0, 1, 0), 'R', craftingtable, 'A', alloy2, 'C', active_core1, 'O', obsidian));
        }

        if (Configs.disableRecipeEnergyBridgeTransmitter.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 0), "ASA", "ACA", "AGA", 'S', Items.nether_star, 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeReceiver.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 1), "AGA", "GCG", "AGA", 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeResonator.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 2), "AGA", "GCG", "AAA", 'A', alloy1, 'G', "blockGlass", 'C', active_core1));
        }

        if (Configs.disableRecipeTemplatedChest0.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 2, 0), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy0, 'C', chest);
        }
        if (Configs.disableRecipeTemplatedChest1.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 2, 1), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy1, 'C', chest);
        }
        if (Configs.disableRecipeTemplatedChest2.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 2, 2), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy2, 'C', chest);
        }

        if (Configs.disableRecipeHandyChest0.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 1, 3), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy0, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest1.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 1, 4), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy1, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest2.getBoolean(false) == false && Configs.disableBlockStorage0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(storage_0, 1, 5), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy2, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
    }
}
