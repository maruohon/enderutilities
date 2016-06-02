package fi.dy.masa.enderutilities.setup;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import fi.dy.masa.enderutilities.block.BlockElevator;
import fi.dy.masa.enderutilities.block.BlockEnderFurnace;
import fi.dy.masa.enderutilities.block.BlockEnergyBridge;
import fi.dy.masa.enderutilities.block.BlockFrame;
import fi.dy.masa.enderutilities.block.BlockMachine;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesPortal;
import fi.dy.masa.enderutilities.block.BlockPortalPanel;
import fi.dy.masa.enderutilities.block.BlockStorage;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.block.base.ItemBlockEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class EnderUtilitiesBlocks
{
    public static final BlockEnderUtilities blockElevator       = new BlockElevator(ReferenceNames.NAME_TILE_ENDER_ELEVATOR,    4.0f,   10f, 1, Material.IRON);
    public static final BlockEnderUtilities blockEnergyBridge   = new BlockEnergyBridge(ReferenceNames.NAME_TILE_ENERGY_BRIDGE, 8.0f,  400f, 2, Material.IRON);
    public static final BlockEnderUtilities blockFrame          = new BlockFrame(ReferenceNames.NAME_TILE_FRAME,                4.0f,   20f, 2, Material.GLASS);
    public static final BlockEnderUtilities blockMachine_0      = new BlockEnderFurnace(ReferenceNames.NAME_TILE_MACHINE_0,     6.0f,  400f, 1, Material.IRON);
    public static final BlockEnderUtilities blockMachine_1      = new BlockMachine(ReferenceNames.NAME_TILE_MACHINE_1,          6.0f,  400f, 1, Material.IRON);
    public static final BlockEnderUtilities blockPortal         = new BlockEnderUtilitiesPortal(ReferenceNames.NAME_TILE_PORTAL, 4.0f,  20f, 2, Material.GLASS);
    public static final BlockEnderUtilities blockPortalPanel    = new BlockPortalPanel(ReferenceNames.NAME_TILE_PORTAL_PANEL,   4.0f,   20f, 2, Material.GLASS);
    public static final BlockEnderUtilities blockStorage_0      = new BlockStorage(ReferenceNames.NAME_TILE_STORAGE_0,          6.0f, 1000f, 1, Material.IRON);

    public static void init()
    {
        // Register blocks
        registerBlock(blockElevator,        Configs.disableBlockEnderElevator);
        registerBlock(blockFrame,           Configs.disableBlockFrame);
        registerBlock(blockEnergyBridge,    Configs.disableBlockEnergyBridge);
        registerBlock(blockMachine_0,       Configs.disableBlockMachine_0);
        registerBlock(blockMachine_1,       Configs.disableBlockMachine_1);
        registerBlock(blockPortal,          false, true); // FIXME disable item
        registerBlock(blockPortalPanel,     Configs.disableBlockPortalPanel);
        registerBlock(blockStorage_0,       Configs.disableBlockStorage_0);

        ItemStack chest = new ItemStack(Blocks.CHEST);
        ItemStack craftingtable = new ItemStack(Blocks.CRAFTING_TABLE);
        ItemStack enderChest = new ItemStack(Blocks.ENDER_CHEST);
        ItemStack furnace = new ItemStack(Blocks.FURNACE);
        ItemStack hopper = new ItemStack(Blocks.HOPPER);
        ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
        ItemStack piston = new ItemStack(Blocks.PISTON);
        ItemStack repeater = new ItemStack(Items.REPEATER);

        ItemStack alloy0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 0);
        ItemStack alloy1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 1);
        ItemStack alloy2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 2);
        ItemStack core0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 10);
        ItemStack core1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 11);
        //ItemStack core2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 12);
        //ItemStack active_core0 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 15);
        ItemStack active_core1 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 16);
        ItemStack active_core2 = new ItemStack(EnderUtilitiesItems.enderPart, 1, 17);
        //ItemStack ender_stick = new ItemStack(EnderUtilitiesItems.enderPart, 1, 20);
        //ItemStack rope = new ItemStack(EnderUtilitiesItems.enderPart, 1, 21);

        // Register block recipes
        if (Configs.disableRecipeEnderElevator == false && Configs.disableBlockEnderElevator == false)
        {
            for (EnumDyeColor color : EnumDyeColor.values())
            {
                int meta = color.getMetadata();
                GameRegistry.addRecipe(new ItemStack(blockElevator, 2, meta), "WSW", "APA", "AAA", 'W', new ItemStack(Blocks.WOOL, 1, meta), 'A', alloy1, 'S', Blocks.SLIME_BLOCK, 'P', Blocks.STICKY_PISTON);
            }
        }

        if (Configs.disableRecipeFrame == false && Configs.disableBlockFrame == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockFrame, 4, 0), "GAG", "APA", "GAG", 'G', "blockGlass", 'A', alloy0, 'P', Items.ENDER_PEARL));
        }

        if (Configs.disableRecipeEnderFurnace == false && Configs.disableBlockMachine_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockMachine_0, 1, 0), "OAO", "AFA", "OCO", 'O', obsidian, 'A', alloy1, 'F', furnace, 'C', core0);
        }

        if (Configs.disableRecipeEnderInfuser == false && Configs.disableBlockMachine_1 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockMachine_1, 1, 0), "AHA", "APA", "OFO", 'A', alloy0, 'H', hopper, 'P', piston, 'O', obsidian, 'F', furnace);
        }
        if (Configs.disableRecipeToolWorkstation == false && Configs.disableBlockMachine_1 == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine_1, 1, 1), "ASA", "ACA", "OHO", 'A', alloy0, 'S', "slimeball", 'C', craftingtable, 'O', obsidian, 'H', chest));
        }
        if (Configs.disableRecipeCreationStation == false && Configs.disableBlockMachine_1 == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine_1, 1, 2), "FRF", "ACA", "OAO", 'F', new ItemStack(blockMachine_0, 1, 0), 'R', craftingtable, 'A', alloy1, 'C', core1, 'O', obsidian));
        }
        if (Configs.disableRecipeAdvancedQuickStacker == false && Configs.disableBlockMachine_1 == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine_1, 1, 3), "AAA", "AQA", "AAA", 'A', alloy0, 'Q', EnderUtilitiesItems.quickStacker));
        }

        if (Configs.disableRecipeEnergyBridgeResonator == false && Configs.disableBlockEnergyBridge == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 0), "AGA", "GCG", "AAA", 'A', alloy1, 'G', "blockGlass", 'C', active_core1));
        }
        if (Configs.disableRecipeEnergyBridgeReceiver == false && Configs.disableBlockEnergyBridge == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 1), "AGA", "GCG", "AGA", 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }
        if (Configs.disableRecipeEnergyBridgeTransmitter == false && Configs.disableBlockEnergyBridge == false)
        {
            GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockEnergyBridge, 1, 2), "ASA", "ACA", "AGA", 'S', Items.NETHER_STAR, 'A', alloy2, 'G', "blockGlass", 'C', active_core2));
        }

        if (Configs.disableRecipeMemoryChest_0 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 0), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy0, 'C', chest);
        }
        if (Configs.disableRecipeMemoryChest_1 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 1), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy1, 'C', chest);
        }
        if (Configs.disableRecipeMemoryChest_2 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 2, 2), "RAR", "ACA", "RAR", 'R', repeater, 'A', alloy2, 'C', chest);
        }

        if (Configs.disableRecipeHandyChest_0 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 3), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy0, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest_1 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 4), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy1, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
        if (Configs.disableRecipeHandyChest_2 == false && Configs.disableBlockStorage_0 == false)
        {
            GameRegistry.addRecipe(new ItemStack(blockStorage_0, 1, 5), "PAP", "ACA", "ROR", 'P', piston, 'A', alloy2, 'C', enderChest, 'O', active_core1, 'R', repeater);
        }
    }

    private static void registerBlock(Block block, boolean isDisabled)
    {
        registerBlock(block, isDisabled, true);
    }

    private static void registerBlock(Block block, boolean isDisabled, boolean createItemBlock)
    {
        if (isDisabled == false)
        {
            GameRegistry.register(block);

            if (createItemBlock)
            {
                GameRegistry.register(new ItemBlockEnderUtilities(block).setRegistryName(block.getRegistryName()));
            }
        }
    }
}
