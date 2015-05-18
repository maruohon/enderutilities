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
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityProjectile;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.event.ModelEventHandler;
import fi.dy.masa.enderutilities.init.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.item.tool.ItemEnderSword;
import fi.dy.masa.enderutilities.item.tool.ItemEnderTool;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.setup.Configs;
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
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class, new RenderEndermanFighter(rm));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEnergyBridge.class, new TileEntityRendererEnergyBridge());
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

    @Override
    public void preInit()
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderPart.registerVariants();
        }
        if (Configs.disableItemEnderCapacitor.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderCapacitor.registerVariants();
        }
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            EnderUtilitiesItems.linkCrystal.registerVariants();
        }
        if (Configs.disableItemEnderArrow.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderArrow.registerVariants();
        }
        if (Configs.disableItemEnderBag.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderBag.registerVariants();
        }
        if (Configs.disableItemEnderBow.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderBow.registerVariants();
        }
        if (Configs.disableItemEnderBucket.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderBucket.registerVariants();
        }
        if (Configs.disableItemEnderLasso.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderLasso.registerVariants();
        }
        if (Configs.disableItemEnderPearl.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderPearlReusable.registerVariants();
        }
        if (Configs.disableItemEnderPorter.getBoolean(false) == false)
        {
            EnderUtilitiesItems.enderPorter.registerVariants();
        }
        if (Configs.disableItemEnderSword.getBoolean(false) == false)
        {
            ((ItemEnderSword)EnderUtilitiesItems.enderSword).registerVariants();
        }
        if (Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            ((ItemEnderTool)EnderUtilitiesItems.enderTool).registerVariants();
        }
        if (Configs.disableItemMobHarness.getBoolean(false) == false)
        {
            EnderUtilitiesItems.mobHarness.registerVariants();
        }
    }
}
