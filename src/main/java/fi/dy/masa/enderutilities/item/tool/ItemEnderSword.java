package fi.dy.masa.enderutilities.item.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceMaterial;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderSword extends ItemSword implements IKeyBound, IModular
{
    private float damageVsEntity;
    private final Item.ToolMaterial material;

    /** Non-namespaced/non-mod-domain-prepended variant names for this item. */
    @SideOnly(Side.CLIENT)
    public String variants[];
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite textures[];
    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel models[];


    public ItemEnderSword()
    {
        super(ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED);
        this.material = ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setMaxDamage(this.material.getMaxUses());
        this.setNoRepair();
        this.damageVsEntity = 6.0f + this.material.getDamageVsEntity();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setUnlocalizedName(ReferenceNames.getPrefixedName(ReferenceNames.NAME_ITEM_ENDER_SWORD));
    }

    // This is used for determining which weapon is better when mobs pick up items
    @Override
    public float getDamageVsEntity()
    {
        // FIXME no way to check if the item is broken without ItemStack and NBT data
        return this.damageVsEntity;
    }

    /**
     * Return the maxDamage for this ItemStack. Defaults to the maxDamage field in this item, 
     * but can be overridden here for other sources such as NBT.
     *
     * @param stack The itemstack that is damaged
     * @return the damage value
     */
    @Override
    public int getMaxDamage(ItemStack stack)
    {
        /**
         * Returns the maximum damage an item can take.
         */
        //return this.material.getMaxUses();
        return 5;
    }

    public boolean isToolBroken(ItemStack stack)
    {
        if (stack == null || stack.getItemDamage() >= this.getMaxDamage(stack))
        {
            return true;
        }

        return false;
    }

    @Override
    public float getStrVsBlock(ItemStack stack, Block block)
    {
        if (this.isToolBroken(stack) == true)
        {
            return 0.2f;
        }

        if (block == Blocks.web)
        {
            return 15.0f;
        }

        Material material = block.getMaterial();
        if (material == Material.plants ||
            material == Material.vine ||
            material == Material.coral ||
            material == Material.leaves ||
            material == Material.gourd)
        {
            return 1.5f;
        }

        return 1.0f;
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
    {
        if (this.isToolBroken(stack) == false)
        {
            stack.damageItem(1, living1);

            // Tool just broke
            if (this.isToolBroken(stack) == true)
            {
                living1.renderBrokenItemStack(stack);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, BlockPos pos, EntityLivingBase livingbase)
    {
        if (block.getBlockHardness(world, pos) != 0.0f && this.isToolBroken(stack) == false)
        {
            int amount = Math.min(2, this.getMaxDamage(stack) - stack.getItemDamage());
            stack.damageItem(amount, livingbase);

            // Tool just broke
            if (this.isToolBroken(stack) == true)
            {
                livingbase.renderBrokenItemStack(stack);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean isItemTool(ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean isRepairable()
    {
        return false;
    }

    /**
     * ItemStack sensitive version of getItemEnchantability
     * 
     * @param stack The ItemStack
     * @return the item echantability value
     */
    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    /**
     * Returns True is the item is renderer in full 3D when hold.
     */
    @SideOnly(Side.CLIENT)
    public boolean isFull3D()
    {
        return true;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BLOCK;
    }

    /**
     * How long it takes to use or consume an item
     */
    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return block == Blocks.web;
    }

    /**
     * Return whether this item is repairable in an anvil.
     */
    @Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        return false;
    }

    /**
     * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
     */
    @Override
    public Multimap getAttributeModifiers(ItemStack stack)
    {
        double dmg = this.damageVsEntity;
        if (this.isToolBroken(stack) == true)
        {
            dmg = 1.0d;
        }

        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(Item.itemModifierUUID, "Weapon modifier", dmg, 0));
        return multimap;
    }

    private byte getToolMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        return nbt.getByte("ToolMode");
    }

    public void toggleToolMode(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        byte mode = this.getToolMode(stack);

        // Ender Sword has 3 modes: Normal (= insert to player inventory), Send (= send to bound inventory) and Summon
        if (++mode > 2)
        {
            mode = 0;
        }

        nbt.setByte("ToolMode", mode);
        stack.setTagCompound(nbt);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null)
        {
            return;
        }

        if (ReferenceKeys.getBaseKey(key) == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.toggleToolMode(stack);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items without NBT data: display the tips before the usual tooltip data
        if (stack != null && stack.getTagCompound() == null)
        {
            this.addTooltips(stack, tmpList, verbose);

            if (verbose == false && tmpList.size() > 1)
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.holdshiftfordescription"));
            }
            else
            {
                list.addAll(tmpList);
            }
        }

        /*tmpList.clear();
        this.addInformationSelective(stack, player, tmpList, advancedTooltips, true);

        // If we want the compact version of the tooltip, and the compact list has more than 2 lines, only show the first line
        // plus the "Hold Shift for more" tooltip.
        if (verbose == false && tmpList.size() > 2)
        {
            tmpList.clear();
            this.addInformationSelective(stack, player, tmpList, advancedTooltips, false);
            list.add(tmpList.get(0));
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.holdshift"));
        }
        else
        {
            list.addAll(tmpList);
        }*/
        //list.add(StatCollector.translateToLocal("enderutilities.tooltip.durability") + ": " + (this.getMaxDamage(stack) - this.getDamage(stack) + " / " + this.getMaxDamage(stack)));

        super.addInformation(stack, player, list, advancedTooltips);
    }

    @SideOnly(Side.CLIENT)
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        ItemEnderUtilities.addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    /* Returns the number of installed modules of the given type. */
    @Override
    public int getModuleCount(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getModuleCount(stack, moduleType);
    }

    /* Returns the maximum number of modules that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 3;
    }

    /* Returns the maximum number of modules of the given type that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCORE_ACTIVE))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL))
        {
            return 1;
        }

        return 0;
    }

    /* Returns the maximum number of the given module that can be installed on this item.
     * This is for exact module checking, instead of the general module type. */
    @Override
    public int getMaxModules(ItemStack toolStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);

        // Allow the in-world/location and block/inventory type Link Crystals
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == false
            || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_LOCATION
            || imodule.getModuleTier(moduleStack) == ItemLinkCrystal.TYPE_BLOCK)
        {
            return this.getMaxModules(toolStack, moduleType);
        }

        return 0;
    }

    /* Returns the (max, if multiple) tier of the installed module. */
    @Override
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(stack, moduleType);
    }

    /* Returns the tier of the selected module of the given type. */
    public int getSelectedModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(stack, moduleType);
    }

    /* Returns the ItemStack of the (selected, if multiple) given module type. */
    @Override
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(stack, moduleType);
    }

    /* Sets the selected modules' ItemStack of the given module type to the one provided. */
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);
    }

    /* Change the selected module to the next one, if any. */
    @Override
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse)
    {
        return stack;
    }

    /* Returns a list of all the installed modules. */
    @Override
    public List<NBTTagCompound> getAllModules(ItemStack stack)
    {
        return UtilItemModular.getAllModules(stack);
    }

    /* Sets the modules to the ones provided in the list. */
    @Override
    public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
    {
        return UtilItemModular.setAllModules(stack, modules);
    }

    /* Sets the module indicated by the position to the one provided in the compound tag. */
    @Override
    public ItemStack setModule(ItemStack stack, int index, NBTTagCompound nbt)
    {
        return UtilItemModular.setModule(stack, index, nbt);
    }

    @SideOnly(Side.CLIENT)
    public void addVariants(String... variantsIn)
    {
        // FIXME we should register all _models_ not individual texture names
        // That would also mean fixing the models so that a single model has all the necessary layers for each item
        // and the face quads should be baked based on the item NBT.
        int len = variantsIn.length;
        this.variants = new String[len];

        String[] namespaced = new String[len];
        for (int i = 0; i < len; ++i)
        {
            this.variants[i] = variantsIn[i];
            namespaced[i] = Reference.MOD_ID + ":" + variantsIn[i];
        }

        ModelBakery.addVariantName(this, namespaced);
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        int len = this.variants.length;
        this.textures = new TextureAtlasSprite[len];

        for (int i = 0; i < len; ++i)
        {
            String name = ReferenceTextures.getItemTextureName(this.variants[i]);
            //textureMap.setTextureEntry(name, new EnderUtilitiesTexture(name));
            //this.textures[i] = textureMap.getTextureExtry(name);
            textureMap.registerSprite(new ResourceLocation(name));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher, TextureMap textureMap, Map<ResourceLocation, ModelBlock> modelMap)
    {
        itemModelMesher.register(this, EnderUtilitiesModelRegistry.baseItemMeshDefinition);
        ItemModelGenerator itemModelGenerator = new ItemModelGenerator();

        int len = this.variants.length;
        this.models = new IFlexibleBakedModel[len];

        ModelBlock base;

        String name = Reference.MOD_ID + ":item/" + ReferenceNames.NAME_ITEM_ENDERTOOL;
        base = EnderUtilitiesModelBlock.readModel(new ResourceLocation(name), modelMap);
        if (base == null)
        {
            EnderUtilities.logger.fatal("Failed to read ModelBlock for " + name);
            base = EnderUtilitiesModelRegistry.modelBlockBaseItems;
        }

        for (int i = 0; i < len; ++i)
        {
            String modelName = Reference.MOD_ID + ":item/" + this.variants[i];
            String textureName = ReferenceTextures.getItemTextureName(this.variants[i]);
            ModelBlock modelBlock = EnderUtilitiesModelBlock.createNewItemModelBlockForTexture(base, modelName, textureName, modelMap, true);
            modelBlock = itemModelGenerator.makeItemModel(textureMap, modelBlock);

            if (modelBlock != null)
            {
                this.models[i] = EnderUtilitiesModelFactory.instance.bakeModel(modelBlock, ModelRotation.X0_Y0, false); // FIXME: rotation and uv-lock ??
                modelRegistry.putObject(new ModelResourceLocation(Reference.MOD_ID + ":" + this.variants[i], "inventory"), this.models[i]);
            }
            else
            {
                EnderUtilities.logger.fatal("ModelBlock from makeItemModel() was null when trying to bake item model for " + this.variants[i]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerVariants()
    {
        String[] parts = new String[] {"rod", "head.1", "head.2", "head.3", "head.1.broken", "head.2.broken", "head.3.broken",
                "core.1", "core.2", "core.3", "capacitor.1", "capacitor.2", "capacitor.3", "linkcrystal.1", "linkcrystal.2"};
        //this.iconEmpty = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));

        int len = parts.length;
        String prefix = ReferenceNames.NAME_ITEM_ENDER_SWORD + ".";
        String[] allVariants = new String[len];

        for (int i = 0; i < len; i++)
        {
            allVariants[i] = prefix + parts[i];
        }

        this.addVariants(allVariants);
    }

    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel getItemModel(ItemStack stack)
    {
        if (stack == null)
        {
            return this.models[0];
        }

        int i = getToolMode(stack) + 1; // Head icons start at index 1

        // Broken tool
        if (this.isToolBroken(stack) == true)
        {
            i += 3;
        }

        // Merge the rod and the correct head models
        IFlexibleBakedModel model = EnderUtilitiesModelFactory.mergeModelsSimple(this.models[0], this.models[i]);

        int tier = 0;
        // Core module
        tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
        if (tier >= 0)
        {
            model = EnderUtilitiesModelFactory.mergeModelsSimple(model, this.models[tier + 7]);
        }

        // Capacitor module
        tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCAPACITOR);
        if (tier >= 0)
        {
            model = EnderUtilitiesModelFactory.mergeModelsSimple(model, this.models[tier + 10]);
        }

        // Link Crystal
        ItemStack lcStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (lcStack != null && lcStack.getItem() instanceof ItemLinkCrystal)
        {
            tier = ((ItemLinkCrystal)lcStack.getItem()).getModuleTier(lcStack);
        }
        else
        {
            tier = this.getMaxModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL);
        }

        if (tier >= 0)
        {
            model = EnderUtilitiesModelFactory.mergeModelsSimple(model, this.models[tier + 13]);
        }

        return model;
    }
}
