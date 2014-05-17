package fi.dy.masa.minecraft.mods.enderutilities.init;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.block.EnderFurnace;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public class EnderUtilitiesBlocks
{
	public static final Block enderFurnace = new EnderFurnace();

	public static void init()
	{
		GameRegistry.registerBlock(enderFurnace, Reference.NAME_BLOCK_ENDER_FURNACE);
	}
}
