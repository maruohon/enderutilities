package fi.dy.masa.minecraft.mods.enderutilities.proxy;

import net.minecraft.tileentity.TileEntityFurnace;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.minecraft.mods.enderutilities.EnderUtilities;
import fi.dy.masa.minecraft.mods.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.minecraft.mods.enderutilities.reference.Reference;

public abstract class CommonProxy implements IProxy
{
	public void registerEntities()
	{
		int id = 0;
		EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, Reference.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 10, true);
	}

	public void registerTileEntities()
	{
		// FIXME: create my own tile entity?
		GameRegistry.registerTileEntity(TileEntityFurnace.class, Reference.NAME_TILE_ENDER_FURNACE);
	}
}
