package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.event.AnvilUpdateEventHandler;
import fi.dy.masa.enderutilities.event.AttackEntityEventHandler;
import fi.dy.masa.enderutilities.event.BlockEventHandler;
import fi.dy.masa.enderutilities.event.EntityInteractEventHandler;
import fi.dy.masa.enderutilities.event.FMLPlayerEventHandler;
import fi.dy.masa.enderutilities.event.PlayerEventHandler;
import fi.dy.masa.enderutilities.event.TickHandler;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.ChunkLoading;

public abstract class CommonProxy implements IProxy
{
    @Override
    public void preInit()
    {
    }

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

    @Override
    public void registerEntities()
    {
        int id = 0;
        EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceNames.getPrefixedName(ReferenceNames.NAME_ENTITY_ENDER_ARROW), id++, EnderUtilities.instance, 64, 3, true);
        EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceNames.getPrefixedName(ReferenceNames.NAME_ENTITY_ENDER_PEARL_REUSABLE), id++, EnderUtilities.instance, 64, 3, true);
    }

    @Override
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(new AnvilUpdateEventHandler());
        MinecraftForge.EVENT_BUS.register(new AttackEntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
        MinecraftForge.EVENT_BUS.register(new EntityInteractEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
        FMLCommonHandler.instance().bus().register(new TickHandler());
        FMLCommonHandler.instance().bus().register(new FMLPlayerEventHandler());
        ForgeChunkManager.setForcedChunkLoadingCallback(EnderUtilities.instance, new ChunkLoading());
    }

    @Override
    public void registerFuelHandlers()
    {
        //GameRegistry.registerFuelHandler(new FuelHandler());
    }

    @Override
    public void registerTileEntities()
    {
        // TODO: Remove the WithAlternatives version of the register call after some releases. Added in v0.4.0 to be able to prefix the TE names with the MOD_ID.
        GameRegistry.registerTileEntity(TileEntityEnderFurnace.class, ReferenceNames.getPrefixedName(ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE));
        GameRegistry.registerTileEntity(TileEntityToolWorkstation.class, ReferenceNames.getPrefixedName(ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION));
        GameRegistry.registerTileEntity(TileEntityEnderInfuser.class, ReferenceNames.getPrefixedName(ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER));
    }

    @Override
    public void registerKeyBindings()
    {
    }

    @Override
    public void registerRenderers()
    {
    }

    @Override
    public boolean isShiftKeyDown()
    {
        return false;
    }

    @Override
    public boolean isControlKeyDown()
    {
        return false;
    }

    @Override
    public boolean isAltKeyDown()
    {
        return false;
    }
}
