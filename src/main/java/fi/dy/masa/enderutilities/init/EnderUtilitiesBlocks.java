package fi.dy.masa.enderutilities.init;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.block.BlockEnderFurnace;
import fi.dy.masa.enderutilities.reference.tileentity.ReferenceTileEntity;

public class EnderUtilitiesBlocks
{
	public static final Block enderFurnace = new BlockEnderFurnace();

	public static void init()
	{
		GameRegistry.registerBlock(enderFurnace, ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
		GameRegistry.addRecipe(new ItemStack(enderFurnace), "PDP", "DFD", "EDE",
				'E', new ItemStack(Items.ender_eye), 'D', new ItemStack(Items.diamond), 'F', new ItemStack(Blocks.furnace), 'P', new ItemStack(Items.ender_pearl));
	}
}
