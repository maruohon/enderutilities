package fi.dy.masa.enderutilities.proxy;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.reference.entity.ReferenceEntity;
import fi.dy.masa.enderutilities.reference.tileentity.ReferenceTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public abstract class CommonProxy implements IProxy
{
	public void registerEntities()
	{
		int id = 0;
		EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceEntity.NAME_ENTITY_ENDER_ARROW, id++, EnderUtilities.instance, 64, 10, true);
		EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceEntity.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 10, true);
	}

	public void registerTileEntities()
	{
		// FIXME: create my own tile entity?
		GameRegistry.registerTileEntity(TileEntityEnderFurnace.class, ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
	}
}
