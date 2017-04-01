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
import fi.dy.masa.enderutilities.entity.EntityChair;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.entity.EntityFallingBlockEU;
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
import fi.dy.masa.enderutilities.tileentity.TileEntityASU;
import fi.dy.masa.enderutilities.tileentity.TileEntityBarrel;
import fi.dy.masa.enderutilities.tileentity.TileEntityCreationStation;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderElevator;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderFurnace;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnderInfuser;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;
import fi.dy.masa.enderutilities.tileentity.TileEntityInserter;
import fi.dy.masa.enderutilities.tileentity.TileEntityHandyChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityJSU;
import fi.dy.masa.enderutilities.tileentity.TileEntityMSU;
import fi.dy.masa.enderutilities.tileentity.TileEntityMemoryChest;
import fi.dy.masa.enderutilities.tileentity.TileEntityMolecularExciter;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortal;
import fi.dy.masa.enderutilities.tileentity.TileEntityPortalPanel;
import fi.dy.masa.enderutilities.tileentity.TileEntityQuickStackerAdvanced;
import fi.dy.masa.enderutilities.tileentity.TileEntityToolWorkstation;
import fi.dy.masa.enderutilities.util.ChunkLoading;

public class CommonProxy implements IProxy
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
    public void registerColorHandlers() { }

    @Override
    public void registerEntities()
    {
        int id = 0;
        EntityRegistry.registerModEntity(EntityEnderArrow.class, ReferenceNames.NAME_ENTITY_ENDER_ARROW, id++, EnderUtilities.instance, 64, 2, true);
        EntityRegistry.registerModEntity(EntityEnderPearlReusable.class, ReferenceNames.NAME_ENTITY_ENDER_PEARL_REUSABLE, id++, EnderUtilities.instance, 64, 2, true);
        EntityRegistry.registerModEntity(EntityEndermanFighter.class, ReferenceNames.NAME_ENTITY_ENDERMAN_FIGHTER, id++, EnderUtilities.instance, 64, 3, true, 0x161616, 0x1947dc);
        EntityRegistry.registerModEntity(EntityChair.class, ReferenceNames.NAME_ENTITY_CHAIR, id++, EnderUtilities.instance, 64, 10, false);
        EntityRegistry.registerModEntity(EntityFallingBlockEU.class, ReferenceNames.NAME_ENTITY_FALLING_BLOCK, id++, EnderUtilities.instance, 64, 2, false);
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
    public void registerKeyBindings() { }

    @Override
    public void registerModels() { }

    @Override
    public void registerRenderers() { }

    @Override
    public void registerSounds()
    {
        this.registerSound("jailer");
        this.registerSound("molecular_exciter");
    }

    private void registerSound(String name)
    {
        ResourceLocation resloc = new ResourceLocation(Reference.MOD_ID, name);
        GameRegistry.register(new SoundEvent(resloc), resloc);
    }

    @Override
    public void registerTileEntities()
    {
        this.registerTileEntityWithAlternatives(TileEntityCreationStation.class,        ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);
        this.registerTileEntityWithAlternatives(TileEntityEnderElevator.class,          ReferenceNames.NAME_TILE_ENDER_ELEVATOR);
        this.registerTileEntityWithAlternatives(TileEntityEnderFurnace.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE);
        this.registerTileEntityWithAlternatives(TileEntityEnderInfuser.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        this.registerTileEntityWithAlternatives(TileEntityEnergyBridge.class,           ReferenceNames.NAME_TILE_ENTITY_ENERGY_BRIDGE);
        this.registerTileEntityWithAlternatives(TileEntityHandyChest.class,             ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);
        this.registerTileEntityWithAlternatives(TileEntityMemoryChest.class,            ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        this.registerTileEntityWithAlternatives(TileEntityPortal.class,                 ReferenceNames.NAME_TILE_PORTAL);
        this.registerTileEntityWithAlternatives(TileEntityPortalPanel.class,            ReferenceNames.NAME_TILE_PORTAL_PANEL);
        this.registerTileEntityWithAlternatives(TileEntityQuickStackerAdvanced.class,   ReferenceNames.NAME_TILE_ENTITY_QUICK_STACKER_ADVANCED);
        this.registerTileEntityWithAlternatives(TileEntityToolWorkstation.class,        ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);

        this.registerTileEntity(TileEntityASU.class,                    ReferenceNames.NAME_TILE_ENTITY_ASU);
        this.registerTileEntity(TileEntityBarrel.class,                 ReferenceNames.NAME_TILE_ENTITY_BARREL);
        this.registerTileEntity(TileEntityInserter.class,               ReferenceNames.NAME_TILE_INSERTER);
        this.registerTileEntity(TileEntityJSU.class,                    ReferenceNames.NAME_TILE_ENTITY_JSU);
        this.registerTileEntity(TileEntityMolecularExciter.class,       ReferenceNames.NAME_TILE_ENTITY_MOLECULAR_EXCITER);
        this.registerTileEntity(TileEntityMSU.class,                    ReferenceNames.NAME_TILE_ENTITY_MSU);
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

    private void registerTileEntityWithAlternatives(Class<? extends TileEntity> clazz, String id)
    {
        // TODO Remove at some point
        String oldName = Reference.MOD_ID + "." + id.replaceAll("_", ".");
        GameRegistry.registerTileEntityWithAlternatives(clazz, Reference.MOD_ID + ":" + id, ReferenceNames.getPrefixedName(id), oldName);
    }

    private void registerTileEntity(Class<? extends TileEntity> clazz, String id)
    {
        GameRegistry.registerTileEntity(clazz, Reference.MOD_ID + ":" + id);
    }
}
