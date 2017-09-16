package fi.dy.masa.enderutilities.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import fi.dy.masa.enderutilities.block.*;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.recipes.ShapedMetadataOreRecipe;
import fi.dy.masa.enderutilities.tileentity.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class EnderUtilitiesBlocks
{
    public static final BlockEnderUtilities ASU                 = new BlockASU(ReferenceNames.NAME_TILE_ASU,                            6.0f, 20f, 1, Material.IRON);
    public static final BlockEnderUtilities BARREL              = new BlockBarrel(ReferenceNames.NAME_TILE_BARREL,                      4.0f, 10f, 1, Material.IRON);
    public static final BlockEnderUtilities DRAWBRIDGE          = new BlockDrawbridge(ReferenceNames.NAME_TILE_DRAW_BRIDGE,             4.0f, 10f, 1, Material.IRON);
    public static final BlockEnderUtilities ELEVATOR            = new BlockElevator(ReferenceNames.NAME_TILE_ENDER_ELEVATOR,            4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities ELEVATOR_SLAB       = new BlockElevatorSlab(ReferenceNames.NAME_TILE_ENDER_ELEVATOR_SLAB,   4.0f, 10f, 1, Material.ROCK);
    public static final BlockEnderUtilities ELEVATOR_LAYER      = new BlockElevatorSlab(ReferenceNames.NAME_TILE_ENDER_ELEVATOR_LAYER,  4.0f, 10f, 1, Material.ROCK);
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

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        IForgeRegistry<Block> registry = event.getRegistry();

        // Register blocks
        registerBlock(registry, ASU,                    Configs.disableBlockASU);
        registerBlock(registry, BARREL,                 Configs.disableBlockBarrel);
        registerBlock(registry, DRAWBRIDGE,             Configs.disableBlockDrawbridge);
        registerBlock(registry, ELEVATOR,               Configs.disableBlockEnderElevator);
        registerBlock(registry, ELEVATOR_SLAB,          Configs.disableBlockEnderElevator);
        registerBlock(registry, ELEVATOR_LAYER,         Configs.disableBlockEnderElevator);
        registerBlock(registry, INSERTER,               Configs.disableBlockInserter);
        registerBlock(registry, FLOOR,                  Configs.disableBlockFloor);
        registerBlock(registry, ENERGY_BRIDGE,          Configs.disableBlockEnergyBridge);
        registerBlock(registry, ENDER_FURNACE,          Configs.disableBlockMachine_0);
        registerBlock(registry, MACHINE_1,              Configs.disableBlockMachine_1);
        registerBlock(registry, MOLECULAR_EXCITER,      Configs.disableBlockMolecularExciter);
        registerBlock(registry, MSU,                    Configs.disableBlockMSU);
        registerBlock(registry, PHASING,                Configs.disableBlockPhasing);
        registerBlock(registry, PORTAL,                 Configs.disableBlockPortal);
        registerBlock(registry, PORTAL_FRAME,           Configs.disableBlockPortalFrame);
        registerBlock(registry, PORTAL_PANEL,           Configs.disableBlockPortalPanel);
        registerBlock(registry, SOUND_BLOCK,            Configs.disableBlockSoundBlock);
        registerBlock(registry, STORAGE_0,              Configs.disableBlockStorage_0);

        registerTileEntities();
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event)
    {
        IForgeRegistry<Item> registry = event.getRegistry();

        // Register ItemBlocks
        registerItemBlock(registry, ASU,                    Configs.disableBlockASU);
        registerItemBlock(registry, BARREL,                 Configs.disableBlockBarrel);
        registerItemBlock(registry, DRAWBRIDGE,             Configs.disableBlockDrawbridge);
        registerItemBlock(registry, ELEVATOR,               Configs.disableBlockEnderElevator);
        registerItemBlock(registry, ELEVATOR_SLAB,          Configs.disableBlockEnderElevator);
        registerItemBlock(registry, ELEVATOR_LAYER,         Configs.disableBlockEnderElevator);
        registerItemBlock(registry, INSERTER,               Configs.disableBlockInserter);
        registerItemBlock(registry, FLOOR,                  Configs.disableBlockFloor);
        registerItemBlock(registry, ENERGY_BRIDGE,          Configs.disableBlockEnergyBridge);
        registerItemBlock(registry, ENDER_FURNACE,          Configs.disableBlockMachine_0);
        registerItemBlock(registry, MACHINE_1,              Configs.disableBlockMachine_1);
        registerItemBlock(registry, MOLECULAR_EXCITER,      Configs.disableBlockMolecularExciter, false);
        registerItemBlock(registry, MSU,                    Configs.disableBlockMSU);
        registerItemBlock(registry, PHASING,                Configs.disableBlockPhasing);
        // No ItemBlock for PORTAL
        registerItemBlock(registry, PORTAL_FRAME,           Configs.disableBlockPortalFrame, false);
        registerItemBlock(registry, PORTAL_PANEL,           Configs.disableBlockPortalPanel, false);
        registerItemBlock(registry, SOUND_BLOCK,            Configs.disableBlockSoundBlock);
        registerItemBlock(registry, STORAGE_0,              Configs.disableBlockStorage_0);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        if (Configs.disableRecipeEnderElevator == false && Configs.disableBlockEnderElevator == false)
        {
            IForgeRegistry<IRecipe> registry = event.getRegistry();

            ResourceLocation name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_crafting");

            //RecipeSorter.register(Reference.MOD_ID + ":shapedmetadataore", ShapedMetadataOreRecipe.class, RecipeSorter.Category.SHAPED, "");

            registry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR, 2), Blocks.WOOL, 0,
                    "WLW", "APA", "ARA",
                    'W', Blocks.WOOL,
                    'A', new ItemStack(EnderUtilitiesItems.ENDER_PART, 1, 0), // Alloy 0
                    'L', Blocks.STONE_PRESSURE_PLATE,
                    'P', Blocks.STICKY_PISTON,
                    'R', Items.REDSTONE));

            // Elevator to Elevator slab and back
            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_normal_to_slab");
            registry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_SLAB, 2), ELEVATOR, 0, "EE", 'E', ELEVATOR));

            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_slab_to_normal");
            registry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR, 2), ELEVATOR_SLAB, 0, "E", "E", 'E', ELEVATOR_SLAB));

            // Elevator Slab to Elevator Layer and back
            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_slab_to_layer");
            registry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_LAYER, 2), ELEVATOR_SLAB, 0, "EE", 'E', ELEVATOR_SLAB));

            name = new ResourceLocation(Reference.MOD_ID, "ender_elevator_layer_to_slab");
            registry.register(new ShapedMetadataOreRecipe(name, new ItemStack(ELEVATOR_SLAB, 2), ELEVATOR_LAYER, 0, "E", "E", 'E', ELEVATOR_LAYER));
        }
    }

    private static void registerTileEntities()
    {
        registerTileEntity(TileEntityASU.class,                    ReferenceNames.NAME_TILE_ASU);
        registerTileEntity(TileEntityBarrel.class,                 ReferenceNames.NAME_TILE_BARREL);
        registerTileEntity(TileEntityCreationStation.class,        ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);
        registerTileEntity(TileEntityDrawbridge.class,             ReferenceNames.NAME_TILE_DRAW_BRIDGE);
        registerTileEntity(TileEntityElevator.class,               ReferenceNames.NAME_TILE_ENDER_ELEVATOR);
        registerTileEntity(TileEntityEnderFurnace.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE);
        registerTileEntity(TileEntityEnderInfuser.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        registerTileEntity(TileEntityEnergyBridge.class,           ReferenceNames.NAME_TILE_ENERGY_BRIDGE);
        registerTileEntity(TileEntityHandyChest.class,             ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);
        registerTileEntity(TileEntityInserter.class,               ReferenceNames.NAME_TILE_INSERTER);
        registerTileEntity(TileEntityJSU.class,                    ReferenceNames.NAME_TILE_ENTITY_JSU);
        registerTileEntity(TileEntityMemoryChest.class,            ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        registerTileEntity(TileEntityMSU.class,                    ReferenceNames.NAME_TILE_MSU);
        registerTileEntity(TileEntityPortal.class,                 ReferenceNames.NAME_TILE_PORTAL);
        registerTileEntity(TileEntityPortalFrame.class,            ReferenceNames.NAME_TILE_FRAME);
        registerTileEntity(TileEntityPortalPanel.class,            ReferenceNames.NAME_TILE_PORTAL_PANEL);
        registerTileEntity(TileEntityQuickStackerAdvanced.class,   ReferenceNames.NAME_TILE_QUICK_STACKER_ADVANCED);
        registerTileEntity(TileEntitySoundBlock.class,             ReferenceNames.NAME_TILE_SOUND_BLOCK);
        registerTileEntity(TileEntityToolWorkstation.class,        ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
    }

    private static void registerTileEntity(Class<? extends TileEntity> clazz, String id)
    {
        GameRegistry.registerTileEntity(clazz, Reference.MOD_ID + ":" + id);
    }

    private static void registerBlock(IForgeRegistry<Block> registry, BlockEnderUtilities block, boolean isDisabled)
    {
        if (isDisabled == false)
        {
            block.setRegistryName(Reference.MOD_ID + ":" + block.getBlockName());
            registry.register(block);
        }
        else
        {
            block.setEnabled(false);
        }
    }

    private static void registerItemBlock(IForgeRegistry<Item> registry, BlockEnderUtilities block, boolean isDisabled)
    {
        registerItemBlock(registry, block, isDisabled, true);
    }

    private static void registerItemBlock(IForgeRegistry<Item> registry, BlockEnderUtilities block, boolean isDisabled, boolean hasSubtypes)
    {
        if (isDisabled == false)
        {
            Item item = block.createItemBlock().setRegistryName(Reference.MOD_ID, block.getBlockName()).setHasSubtypes(hasSubtypes);
            registry.register(item);
        }
    }
}
