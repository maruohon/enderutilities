package fi.dy.masa.enderutilities.proxy;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Util;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.ReflectionHelper;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityProjectile;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.ItemRendererEnderBucket;
import fi.dy.masa.enderutilities.client.renderer.item.RenderEnderBow;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.GuiEventHandler;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceReflection;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.setup.Keybindings;
import fi.dy.masa.enderutilities.tileentity.TileEntityEnergyBridge;

public class ClientProxy extends CommonProxy
{
    @Override
    public EntityPlayer getPlayerFromMessageContext(MessageContext ctx)
    {
        switch (ctx.side)
        {
            case CLIENT:
                return FMLClientHandler.instance().getClientPlayerEntity();
            case SERVER:
                return ctx.getServerHandler().playerEntity;
            default:
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext(): " + ctx.side);
                return null;
        }
    }

    @Override
    public void registerEventHandlers()
    {
        super.registerEventHandlers();
        InputEventHandler ieh = new InputEventHandler();
        FMLCommonHandler.instance().bus().register(ieh);
        MinecraftForge.EVENT_BUS.register(ieh);
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
    }

    @Override
    public void registerKeyBindings()
    {
        Keybindings.keyToggleMode = new KeyBinding(ReferenceKeys.KEYBIND_NAME_TOGGLE_MODE, ReferenceKeys.DEFAULT_KEYBIND_TOGGLE_MODE, ReferenceKeys.KEYBIND_CAREGORY_ENDERUTILITIES);

        ClientRegistry.registerKeyBinding(Keybindings.keyToggleMode);
    }

    @Override
    public void registerRenderers()
    {
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, new RenderEnderArrow());
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderEntityProjectile(EnderUtilitiesItems.enderPearlReusable));
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class, new RenderEndermanFighter());

        MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBow, new RenderEnderBow());
        MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBucket, new ItemRendererEnderBucket());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyBridge.class, new TileEntityRendererEnergyBridge());
        MinecraftForge.EVENT_BUS.register(new BuildersWandRenderer());
        MinecraftForge.EVENT_BUS.register(new RulerRenderer());
    }

    @Override
    public void setupReflection()
    {
        ReferenceReflection.fieldGuiContainerTheSlot = ReflectionHelper.findField(GuiContainer.class, "u", "field_147006_u", "theSlot");
    }

    @Override
    public boolean isShiftKeyDown()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public boolean isControlKeyDown()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
                || (Util.getOSType() == Util.EnumOS.OSX && ((Keyboard.isKeyDown(28) && Keyboard.getEventCharacter() == 0) || Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220)));
    }

    @Override
    public boolean isAltKeyDown()
    {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }
}
