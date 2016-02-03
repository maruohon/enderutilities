package fi.dy.masa.enderutilities.proxy;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.block.base.BlockEnderUtilities;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEnderArrow;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEndermanFighter;
import fi.dy.masa.enderutilities.client.renderer.entity.RenderEntityProjectile;
import fi.dy.masa.enderutilities.client.renderer.item.BuildersWandRenderer;
import fi.dy.masa.enderutilities.client.renderer.item.ItemMeshDefinitionWrapper;
import fi.dy.masa.enderutilities.client.renderer.item.RulerRenderer;
import fi.dy.masa.enderutilities.client.renderer.tileentity.TileEntityRendererEnergyBridge;
import fi.dy.masa.enderutilities.entity.EntityEnderArrow;
import fi.dy.masa.enderutilities.entity.EntityEnderPearlReusable;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.event.GuiEventHandler;
import fi.dy.masa.enderutilities.event.InputEventHandler;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceReflection;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesBlocks;
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
        MinecraftForge.EVENT_BUS.register(new InputEventHandler());
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
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();

        RenderingRegistry.registerEntityRenderingHandler(EntityEnderArrow.class, new RenderEnderArrow(renderManager));
        RenderingRegistry.registerEntityRenderingHandler(EntityEnderPearlReusable.class, new RenderEntityProjectile(renderManager, EnderUtilitiesItems.enderPearlReusable, renderItem));
        RenderingRegistry.registerEntityRenderingHandler(EntityEndermanFighter.class, new RenderEndermanFighter(renderManager));

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

    @Override
    public void registerModels()
    {
        this.registerItemBlockModels();
        this.registerAllItemModels();
    }

    public void registerAllItemModels()
    {
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderCapacitor);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPart);
        this.registerItemModelWithVariants(EnderUtilitiesItems.linkCrystal);

        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.buildersWand);
        this.registerItemModel(EnderUtilitiesItems.enderArrow);
        //this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBag);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderBow);
        this.registerItemModel(EnderUtilitiesItems.enderLasso);
        this.registerItemModelWithVariants(EnderUtilitiesItems.enderPearlReusable);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderPorter);
        //this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderSword);
        //this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.enderTool);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.handyBag);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.inventorySwapper);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.livingManipulator);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.mobHarness);
        this.registerItemModelWithVariantsAndMeshDefinition(EnderUtilitiesItems.pickupManager);
        this.registerItemModel(EnderUtilitiesItems.portalScaler);
        this.registerItemModel(EnderUtilitiesItems.ruler);
    }

    public void registerItemModel(ItemEnderUtilities item)
    {
        this.registerItemModel(item, 0);
    }

    public void registerItemModel(ItemEnderUtilities item, int meta)
    {
        ResourceLocation rl = Item.itemRegistry.getNameForObject(item);
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(rl, "inventory"));
    }

    public void registerItemModelWithVariants(ItemEnderUtilities item)
    {
        ResourceLocation[] variants = item.getItemVariants();
        List<ItemStack> items = new ArrayList<ItemStack>();
        item.getSubItems(item, item.getCreativeTab(), items);

        int i = 0;
        for (ItemStack stack : items)
        {
            ModelResourceLocation mrl = new ModelResourceLocation(variants[i++], "inventory");
            ModelLoader.setCustomModelResourceLocation(stack.getItem(), stack.getItemDamage(), mrl);
        }
    }

    public void registerItemModelWithVariantsAndMeshDefinition(ItemEnderUtilities item)
    {
        ModelLoader.registerItemVariants(item, item.getItemVariants());
        ModelLoader.setCustomMeshDefinition(item, ItemMeshDefinitionWrapper.instance());
        //ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getItemVariants()[0], "inventory"));
    }

    public void registerItemBlockModels()
    {
        this.registerItemBlockModel(EnderUtilitiesBlocks.blockMachine_0, 0,  "facing=north,mode=off");

        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockMachine_1,    "facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockEnergyBridge, "active=false,facing=north,type=", "");
        this.registerAllItemBlockModels(EnderUtilitiesBlocks.blockStorage_0,    "facing=north,type=", "");
    }

    public void registerItemBlockModel(BlockEnderUtilities blockIn, int meta, String fullVariant)
    {
        ItemStack stack = new ItemStack(blockIn, 1, meta);
        Item item = stack.getItem();
        if (item == null)
        {
            return;
        }

        ModelResourceLocation mrl = new ModelResourceLocation(Item.itemRegistry.getNameForObject(item), fullVariant);
        ModelLoader.setCustomModelResourceLocation(item, stack.getItemDamage(), mrl);
    }

    public void registerAllItemBlockModels(BlockEnderUtilities blockIn, String variantPre, String variantPost)
    {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        blockIn.getSubBlocks(Item.getItemFromBlock(blockIn), blockIn.getCreativeTabToDisplayOn(), stacks);
        String[] names = blockIn.getUnlocalizedNames();

        for (ItemStack stack : stacks)
        {
            Item item = stack.getItem();
            int damage = stack.getItemDamage();
            ModelResourceLocation mrl = new ModelResourceLocation(Item.itemRegistry.getNameForObject(item), variantPre + names[damage] + variantPost);
            ModelLoader.setCustomModelResourceLocation(item, damage, mrl);
        }
    }
}
