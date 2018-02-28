package fi.dy.masa.enderutilities.proxy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.registries.IForgeRegistry;
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
import fi.dy.masa.enderutilities.util.ChunkLoading;

public class CommonProxy
{
    @SuppressWarnings("deprecation")
    public String format(String key, Object... args)
    {
        return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, args);
    }

    public EntityPlayer getClientPlayer()
    {
        return null;
    }

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

    public void playSound(int soundId, float pitch, float volume, boolean repeat, boolean stop, float x, float y, float z) {}

    public ModFixs getDataFixer()
    {
        // On a server, the DataFixer gets created for and is stored inside MinecraftServer,
        // but in single player the DataFixer is stored in the client Minecraft class
        // over world reloads.
        return FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, EnderUtilities.DATA_FIXER_VERSION);
    }

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

        MinecraftForge.EVENT_BUS.register(this);

        ForgeChunkManager.setForcedChunkLoadingCallback(EnderUtilities.instance, new ChunkLoading());
    }

    public void registerKeyBindings() { }
    public void registerRenderers() { }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event)
    {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();

        this.registerSound(registry, "jailer");
        this.registerSound(registry, "molecular_exciter");
    }

    private void registerSound(IForgeRegistry<SoundEvent> registry, String name)
    {
        ResourceLocation resloc = new ResourceLocation(Reference.MOD_ID, name);
        SoundEvent sound = new SoundEvent(resloc);
        sound.setRegistryName(resloc);
        registry.register(sound);
    }

    public boolean isShiftKeyDown()
    {
        return false;
    }

    public boolean isControlKeyDown()
    {
        return false;
    }

    public boolean isAltKeyDown()
    {
        return false;
    }
}
