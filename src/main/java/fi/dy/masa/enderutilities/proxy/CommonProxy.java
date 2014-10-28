package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.event.AttackEntityEventHandler;
import fi.dy.masa.enderutilities.event.EntityInteractEventHandler;
import fi.dy.masa.enderutilities.event.FMLPlayerEventHandler;
import fi.dy.masa.enderutilities.event.PlayerEventHandler;
import fi.dy.masa.enderutilities.handler.TickHandler;
import fi.dy.masa.enderutilities.reference.ReferenceBlocksItems;
import fi.dy.masa.enderutilities.reference.ReferenceEntities;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.ChunkLoading;

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
				EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext()");
				return null;
		}
	}

	public void registerEntities()
	{
		int id = 0;
		EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceEntities.NAME_ENTITY_ENDER_ARROW, id++, EnderUtilities.instance, 64, 3, true);
		EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceEntities.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 3, true);
	}

	public void registerEventHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new AttackEntityEventHandler());
		MinecraftForge.EVENT_BUS.register(new EntityInteractEventHandler());
		MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
		FMLCommonHandler.instance().bus().register(new TickHandler());
		FMLCommonHandler.instance().bus().register(new FMLPlayerEventHandler());
		ForgeChunkManager.setForcedChunkLoadingCallback(EnderUtilities.instance, new ChunkLoading());
	}

	public void registerFuelHandlers()
	{
		//GameRegistry.registerFuelHandler(new FuelHandler());
	}

	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityEnderFurnace.class, ReferenceBlocksItems.NAME_TILEENTITY_ENDER_FURNACE);
		GameRegistry.registerTileEntity(TileEntityToolWorkstation.class, ReferenceBlocksItems.NAME_TILEENTITY_TOOL_WORKSTATION);
	}

	public boolean isShiftKeyDown()
	{
		return false;
	}
}
