package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
import fi.dy.masa.enderutilities.tileentity.*;
import fi.dy.masa.enderutilities.util.ChunkLoading;

public class CommonProxy implements IProxy
{
    @Override
    public EntityPlayer getClientPlayer()
    {
        return null;
    }

    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case SERVER:
                return ctx.getServerHandler().player;
            default:
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public void playSound(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z)
    {
    }

    @Override
    public void registerColorHandlers() { }

    @Override
    public ModFixs getDataFixer()
    {
        // On a server, the DataFixer gets created for and is stored inside MinecraftServer,
        // but in single player the DataFixer is stored in the client Minecraft class
        // over world reloads.
        return FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, EnderUtilities.DATA_FIXER_VERSION);
    }

    @Override
    public void registerEntities()
    {
        int id = 0;
        this.registerEntity(EntityEnderArrow.class,         ReferenceNames.NAME_ENTITY_ENDER_ARROW,         id++, EnderUtilities.instance, 64, 2, true);
        this.registerEntity(EntityEnderPearlReusable.class, ReferenceNames.NAME_ENTITY_ENDER_PEARL_REUSABLE,id++, EnderUtilities.instance, 64, 2, true);
        this.registerEntity(EntityEndermanFighter.class,    ReferenceNames.NAME_ENTITY_ENDERMAN_FIGHTER,    id++, EnderUtilities.instance, 64, 3, true, 0x161616, 0x1947dc);
        this.registerEntity(EntityChair.class,              ReferenceNames.NAME_ENTITY_CHAIR,               id++, EnderUtilities.instance, 64, 10, false);
        this.registerEntity(EntityFallingBlockEU.class,     ReferenceNames.NAME_ENTITY_FALLING_BLOCK,       id++, EnderUtilities.instance, 64, 2, false);
    }

    private void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates)
    {
        String prefixedName = Reference.MOD_ID + "." + entityName;
        ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, entityName);
        EntityRegistry.registerModEntity(registryName, entityClass, prefixedName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    private void registerEntity(Class<? extends Entity> entityClass, String entityName, int id, Object mod, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggPrimary, int eggSecondary)
    {
        String prefixedName = Reference.MOD_ID + "." + entityName;
        ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, entityName);
        EntityRegistry.registerModEntity(registryName, entityClass, prefixedName, id, mod, trackingRange, updateFrequency, sendsVelocityUpdates, eggPrimary, eggSecondary);
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
        this.registerTileEntity(TileEntityASU.class,                    ReferenceNames.NAME_TILE_ASU);
        this.registerTileEntity(TileEntityBarrel.class,                 ReferenceNames.NAME_TILE_BARREL);
        this.registerTileEntity(TileEntityCreationStation.class,        ReferenceNames.NAME_TILE_ENTITY_CREATION_STATION);
        this.registerTileEntity(TileEntityDrawbridge.class,             ReferenceNames.NAME_TILE_DRAW_BRIDGE);
        this.registerTileEntity(TileEntityElevator.class,               ReferenceNames.NAME_TILE_ENDER_ELEVATOR);
        this.registerTileEntity(TileEntityEnderFurnace.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_FURNACE);
        this.registerTileEntity(TileEntityEnderInfuser.class,           ReferenceNames.NAME_TILE_ENTITY_ENDER_INFUSER);
        this.registerTileEntity(TileEntityEnergyBridge.class,           ReferenceNames.NAME_TILE_ENERGY_BRIDGE);
        this.registerTileEntity(TileEntityHandyChest.class,             ReferenceNames.NAME_TILE_ENTITY_HANDY_CHEST);
        this.registerTileEntity(TileEntityInserter.class,               ReferenceNames.NAME_TILE_INSERTER);
        this.registerTileEntity(TileEntityJSU.class,                    ReferenceNames.NAME_TILE_ENTITY_JSU);
        this.registerTileEntity(TileEntityMemoryChest.class,            ReferenceNames.NAME_TILE_ENTITY_MEMORY_CHEST);
        this.registerTileEntity(TileEntityMSU.class,                    ReferenceNames.NAME_TILE_MSU);
        this.registerTileEntity(TileEntityPortal.class,                 ReferenceNames.NAME_TILE_PORTAL);
        this.registerTileEntity(TileEntityPortalFrame.class,            ReferenceNames.NAME_TILE_FRAME);
        this.registerTileEntity(TileEntityPortalPanel.class,            ReferenceNames.NAME_TILE_PORTAL_PANEL);
        this.registerTileEntity(TileEntityQuickStackerAdvanced.class,   ReferenceNames.NAME_TILE_QUICK_STACKER_ADVANCED);
        this.registerTileEntity(TileEntitySoundBlock.class,             ReferenceNames.NAME_TILE_SOUND_BLOCK);
        this.registerTileEntity(TileEntityToolWorkstation.class,        ReferenceNames.NAME_TILE_ENTITY_TOOL_WORKSTATION);
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
        GameRegistry.registerTileEntity(clazz, Reference.MOD_ID + ":" + id);
    }
}
