package fi.dy.masa.enderutilities.registry;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.block.*;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.recipes.ShapedMetadataOreRecipe;

public class EnderUtilitiesBlocks
{
    public static final BlockEnderUtilities ASU                 = new BlockASU(ReferenceNames.NAME_TILE_ASU,                            6.0f, 20f, 1, Material.IRON);
    public static final BlockEnderUtilities BARREL              = new BlockBarrel(ReferenceNames.NAME_TILE_BARREL,                      4.0f, 10f, 1, Material.IRON);
    public static final BlockEnderUtilities DRAWBRIDGE          = new BlockDrawbridge(ReferenceNames.NAME_TILE_DRAW_BRIDGE,             4.0f, 10f, 1, Material.IRON);
    public static final BlockEnderUtilities ELEVATOR            = new BlockElevator(ReferenceNames.NAME_TILE_ENDER_ELEVATOR,            4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities ELEVATOR_SLAB       = new BlockElevator(ReferenceNames.NAME_TILE_ENDER_ELEVATOR_SLAB,       4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities ELEVATOR_LAYER      = new BlockElevator(ReferenceNames.NAME_TILE_ENDER_ELEVATOR_LAYER,      4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities ENDER_FURNACE       = new BlockEnderFurnace(ReferenceNames.NAME_TILE_MACHINE_0,             6.0f, 20f, 1, Material.IRON);
    public static final BlockEnderUtilities ENERGY_BRIDGE       = new BlockEnergyBridge(ReferenceNames.NAME_TILE_ENERGY_BRIDGE,         8.0f, 20f, 2, Material.IRON);
    public static final BlockEnderUtilities FLOOR               = new BlockFloor(ReferenceNames.NAME_TILE_FLOOR,                        2.0f,  6f, 1, Material.WOOD);
    public static final BlockEnderUtilities INSERTER            = new BlockInserter(ReferenceNames.NAME_TILE_INSERTER,                  2.0f,  6f, 1, Material.ROCK);
    public static final BlockEnderUtilities MACHINE_1           = new BlockMachine(ReferenceNames.NAME_TILE_MACHINE_1,                  6.0f, 20f, 1, Material.IRON);
    public static final BlockEnderUtilities MOLECULAR_EXCITER   = new BlockMolecularExciter(ReferenceNames.NAME_TILE_MOLECULAR_EXCITER, 3.0f, 20f, 1, Material.ROCK);
    public static final BlockEnderUtilities MSU                 = new BlockMSU(ReferenceNames.NAME_TILE_MSU,                            6.0f, 20f, 1, Material.IRON);
    public static final BlockEnderUtilities PHASING             = new BlockPhasing(ReferenceNames.NAME_TILE_PHASING,                    2.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities PORTAL              = new BlockEnderUtilitiesPortal(ReferenceNames.NAME_TILE_PORTAL,        4.0f, 20f, 2, Material.PORTAL);
    public static final BlockEnderUtilities PORTAL_FRAME        = new BlockPortalFrame(ReferenceNames.NAME_TILE_FRAME,                  4.0f, 20f, 2, Material.ROCK);
    public static final BlockEnderUtilities PORTAL_PANEL        = new BlockPortalPanel(ReferenceNames.NAME_TILE_PORTAL_PANEL,           4.0f, 20f, 2, Material.ROCK);
    public static final BlockEnderUtilities SOUND_BLOCK         = new BlockSound(ReferenceNames.NAME_TILE_SOUND_BLOCK,                  4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities STORAGE_0           = new BlockStorage(ReferenceNames.NAME_TILE_STORAGE_0,                  6.0f, 60f, 1, Material.ROCK);

    public static void registerBlocks()
    {
        // Register blocks
        registerBlock(ASU,                  Configs.disableBlockASU);
        registerBlock(BARREL,               Configs.disableBlockBarrel);
        registerBlock(DRAWBRIDGE,           Configs.disableBlockDrawbridge);
        registerBlock(ELEVATOR,             Configs.disableBlockEnderElevator);
        registerBlock(ELEVATOR_SLAB,        Configs.disableBlockEnderElevator);
        registerBlock(ELEVATOR_LAYER,       Configs.disableBlockEnderElevator);
        registerBlock(INSERTER,             Configs.disableBlockInserter);
        registerBlock(FLOOR,                Configs.disableBlockFloor);
        registerBlock(ENERGY_BRIDGE,        Configs.disableBlockEnergyBridge);
        registerBlock(ENDER_FURNACE,        Configs.disableBlockMachine_0);
        registerBlock(MACHINE_1,            Configs.disableBlockMachine_1);
        registerBlock(MOLECULAR_EXCITER,    Configs.disableBlockMolecularExciter, true, false);
        registerBlock(MSU,                  Configs.disableBlockMSU);
        registerBlock(PHASING,              Configs.disableBlockPhasing);
        registerBlock(PORTAL,               Configs.disableBlockPortal, false, false);
        registerBlock(PORTAL_FRAME,         Configs.disableBlockPortalFrame, true, false);
        registerBlock(PORTAL_PANEL,         Configs.disableBlockPortalPanel, true, false);
        registerBlock(SOUND_BLOCK,          Configs.disableBlockSoundBlock);
        registerBlock(STORAGE_0,            Configs.disableBlockStorage_0);
    }

    public static void registerRecipes()
    {
        if (Configs.disableRecipeEnderElevator == false && Configs.disableBlockEnderElevator == false)
        {
            ResourceLocation name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_crafting");

            //RecipeSorter.register(Reference.MOD_ID + ":shapedmetadataore", ShapedMetadataOreRecipe.class, RecipeSorter.Category.SHAPED, "");

            GameRegistry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR, 2), Blocks.WOOL, 0,
                    "WLW", "APA", "ARA",
                    'W', Blocks.WOOL,
                    'A', new ItemStack(EnderUtilitiesItems.ENDER_PART, 1, 0), // Alloy 0
                    'L', Blocks.STONE_PRESSURE_PLATE,
                    'P', Blocks.STICKY_PISTON,
                    'R', Items.REDSTONE));

            // Elevator to Elevator slab and back
            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_normal_to_slab");
            GameRegistry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_SLAB, 2), ELEVATOR, 0, "EE", 'E', ELEVATOR));

            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_slab_to_normal");
            GameRegistry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR, 2), ELEVATOR_SLAB, 0, "E", "E", 'E', ELEVATOR_SLAB));

            // Elevator Slab to Elevator Layer and back
            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_slab_to_layer");
            GameRegistry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_LAYER, 2), ELEVATOR_SLAB, 0, "EE", 'E', ELEVATOR_SLAB));

            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_layer_to_slab");
            GameRegistry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_SLAB, 2), ELEVATOR_LAYER, 0, "E", "E", 'E', ELEVATOR_LAYER));
        }
    }

    private static void registerBlock(BlockEnderUtilities block, boolean isDisabled)
    {
        registerBlock(block, isDisabled, true);
    }

    private static void registerBlock(BlockEnderUtilities block, boolean isDisabled, boolean createItemBlock)
    {
        registerBlock(block, isDisabled, createItemBlock, true);
    }

    private static void registerBlock(BlockEnderUtilities block, boolean isDisabled, boolean createItemBlock, boolean hasSubtypes)
    {
        if (isDisabled == false)
        {
            block.setRegistryName(Reference.MOD_ID + ":" + block.getBlockName());
            GameRegistry.register(block);

            if (createItemBlock)
            {
                GameRegistry.register(block.createItemBlock().setHasSubtypes(hasSubtypes).setRegistryName(Reference.MOD_ID, block.getBlockName()));
            }
        }
        else
        {
            block.setEnabled(false);
        }
    }
}
