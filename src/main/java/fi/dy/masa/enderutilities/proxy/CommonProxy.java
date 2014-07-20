package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.event.AttackEntityEventHandler;
import fi.dy.masa.enderutilities.event.EntityInteractEventHandler;
import fi.dy.masa.enderutilities.event.PlayerEventHandler;
import fi.dy.masa.enderutilities.reference.entity.ReferenceEntity;
import fi.dy.masa.enderutilities.reference.tileentity.ReferenceTileEntity;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;

public abstract class CommonProxy implements IProxy
{
	@Override
	public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
	{
		switch (ctx.side)
		{
			case SERVER:
				return ctx.getServerHandler().playerEntity;
			default:
				System.out.println("[Ender Utilities] Invalid side in getPlayerFromMessageContext()");
				return null;
		}
	}

	public void registerEntities()
	{
		int id = 0;
		EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceEntity.NAME_ENTITY_ENDER_ARROW, id++, EnderUtilities.instance, 64, 3, true);
		EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceEntity.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 3, true);
	}

	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new AttackEntityEventHandler());
		MinecraftForge.EVENT_BUS.register(new EntityInteractEventHandler());
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
	}

	public void registerTileEntities()
	{
		// FIXME: create my own tile entity?
		GameRegistry.registerTileEntity(TileEntityEnderFurnace.class, ReferenceTileEntity.NAME_TILE_ENDER_FURNACE);
	}

	public boolean isShiftKeyDown()
	{
		return false;
	}
}
