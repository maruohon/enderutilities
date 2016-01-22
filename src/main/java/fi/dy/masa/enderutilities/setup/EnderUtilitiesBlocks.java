package fi.dy.masa.enderutilities.setup;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;

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
        ItemStack furnace = new ItemStack(Blocks.furnace);
        ItemStack hopper = new ItemStack(Blocks.hopper);
        ItemStack obsidian = new ItemStack(Blocks.obsidian);
        ItemStack piston = new ItemStack(Blocks.piston);

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
        if (Configs.disableRecipeToolWorkstation.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_0, 1, 1), "ASA", "ACA", "OHO", 'A', alloy0, 'S', "slimeball", 'C', craftingtable, 'O', obsidian, 'H', chest));
        }
        if (Configs.disableRecipeEnderInfuser.getBoolean(false) == false && Configs.disableBlockMachine_0.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ItemStack(machine_0, 1, 2), "AHA", "APA", "OFO", 'A', alloy0, 'H', hopper, 'P', piston, 'O', obsidian, 'F', furnace);
        }

        if (Configs.disableRecipeEnergyBridgeTransmitter.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 0), "AGA", "ACA", "AGA", 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeReceiver.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 1), "AGA", "GCG", "AGA", 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeResonator.getBoolean(false) == false && Configs.disableBlockMachine_1.getBoolean(false) == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machine_1, 1, 2), "AGA", "GCG", "AAA", 'A', alloy1, 'G', "blockGlass", 'C', active_core1));
        }
    }
}
