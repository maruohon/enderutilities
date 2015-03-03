package fi.dy.masa.enderutilities.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import org.lwjgl.input.Keyboard;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityProjectile;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.event.ModelEventHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.Keybindings;

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
                EnderUtilities.logger.warn("Invalid side in getPlayerFromMessageContext()");
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
        MinecraftForge.EVENT_BUS.register(new ModelEventHandler());
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
        RenderManager rm = Minecraft.getMinecraft().getRenderManager();
        RenderItem ri = Minecraft.getMinecraft().getRenderItem();
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, new RenderEnderArrow(rm));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderEntityProjectile(rm, EnderUtilitiesItems.enderPearlReusable, ri));

        //MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBow, new RenderEnderBow());
        //MinecraftForgeClient.registerItemRenderer(EnderUtilitiesItems.enderBucket, new ItemRendererEnderBucket());

        // FIXME early test stuff:
        //EnderUtilitiesModelRegistry.registerItemModel(ReferenceNames.NAME_ITEM_ENDER_ARROW, 0);
        //EnderUtilitiesModelRegistry.registerItemModel(ReferenceNames.NAME_ITEM_ENDER_LASSO, 0);
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
