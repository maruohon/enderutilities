package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.effects.Sounds;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.AnvilUpdateEventHandler;
import fi.dy.masa.enderutilities.event.BlockEventHandler;
import fi.dy.masa.enderutilities.event.EntityEventHandler;
import fi.dy.masa.enderutilities.event.ItemPickupEventHandler;
import fi.dy.masa.enderutilities.event.LivingDropsEventHandler;
import fi.dy.masa.enderutilities.event.PlayerEventHandler;
import fi.dy.masa.enderutilities.event.TickHandler;
import fi.dy.masa.enderutilities.event.WorldEventHandler;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;
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
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public void registerEntities()
    {
        int id = 0;
        EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceNames.NAME_ENTITY_ENDER_ARROW, id++, EnderUtilities.instance, 64, 2, true);
        EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceNames.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 2, true);
        EntityRegistry.registerModEntity(EntityEndermanFighter.class, ReferenceNames.NAME_ENTITY_ENDERMAN_FIGHTER, id++, EnderUtilities.instance, 64, 3, true);
    }

    @Override
    public void registerEventHandlers()
    {
        MinecraftForge.EVENT_BUS.register(new AnvilUpdateEventHandler());
        MinecraftForge.EVENT_BUS.register(new BlockEventHandler());
        MinecraftForge.EVENT_BUS.register(new EntityEventHandler());
        MinecraftForge.EVENT_BUS.register(new ItemPickupEventHandler());
        MinecraftForge.EVENT_BUS.register(new LivingDropsEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler());
        MinecraftForge.EVENT_BUS.register(new WorldEventHandler());
        MinecraftForge.EVENT_BUS.register(new TickHandler());
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
        this.registerTileEntity(TileEntityEnderFurnace.class,       ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE);
        this.registerTileEntity(TileEntityToolWorkstation.class,    ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
        this.registerTileEntity(TileEntityEnderInfuser.class,       ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        this.registerTileEntity(TileEntityEnergyBridge.class,       ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
        this.registerTileEntity(TileEntityMemoryChest.class,        ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        this.registerTileEntity(TileEntityHandyChest.class,         ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);
        this.registerTileEntity(TileEntityCreationStation.class,    ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);
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
    public void registerSounds()
    {
        ResourceLocation resloc = new ResourceLocation(Reference.MOD_ID, "jailer");
        Sounds.jailer = GameRegistry.register(new SoundEvent(resloc), resloc);
    }

    @Override
    public void setupReflection()
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

    private void registerTileEntity(Class<? extends TileEntity> clazz, String id)
    {
        // TODO Remove at some point
        String oldName = Reference.MOD_ID + "." + id.replaceAll("_", ".");
        GameRegistry.registerTileEntityWithAlternatives(clazz, ReferenceNames.getPrefixedName(id), oldName);
    }
}
