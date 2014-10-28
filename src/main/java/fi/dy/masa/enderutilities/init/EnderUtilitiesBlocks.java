package fi.dy.masa.enderutilities.init;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.block.BlockEnderUtilitiesInventory;
import fi.dy.masa.enderutilities.block.ItemBlockMachine;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.setup.EUConfigs;

public class EnderUtilitiesBlocks
{
	public static final Block machine_0 = new BlockEnderUtilitiesInventory(0, ReferenceBlocksItems.NAME_TILE_MACHINE_0, 1.0f);

	public static void init()
	{
		// Register blocks
		if (EUConfigs.disableBlockMachine_0.getBoolean(false) == false) {
			GameRegistry.registerBlock(machine_0, ItemBlockMachine.class, ReferenceBlocksItems.NAME_TILE_MACHINE_0);
		}

		// Register block recipes
		if (EUConfigs.disableRecipeEnderFurnace.getBoolean(false) == false && EUConfigs.disableBlockMachine_0.getBoolean(false) == false) {
			GameRegistry.addRecipe(new ItemStack(machine_0, 1, 0), "PDP", "DFD", "EDE",
				'E', new ItemStack(Items.ender_eye), 'D', new ItemStack(Items.diamond), 'F', new ItemStack(Blocks.furnace), 'P', new ItemStack(Items.ender_pearl));
		}
	}
}
