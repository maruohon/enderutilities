package fi.dy.masa.enderutilities.item.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.client.effects.Sounds;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceMaterial;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderTool extends ItemTool implements IKeyBound, IModular
{
    public static final int ENDER_CHARGE_COST = 50;
    public float efficiencyOnProperMaterial;
    public float damageVsEntity;
    private final Item.ToolMaterial material;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    @SideOnly(Side.CLIENT)
    private IIcon iconEmpty;
    @SideOnly(Side.CLIENT)
    String[] parts;

    public ItemEnderTool()
    {
        // The Set is not actually used for anything!
        super(2.0f, ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED, Sets.newHashSet(new Block[]{Blocks.torch}));
        this.material = ReferenceMaterial.Tool.ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setMaxDamage(this.material.getMaxUses());
        this.setNoRepair();
        this.efficiencyOnProperMaterial = this.material.getEfficiencyOnProperMaterial();
        this.damageVsEntity = 2.0f + this.material.getDamageVsEntity();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setUnlocalizedName(ReferenceNames.getPrefixedName(ReferenceNames.NAME_ITEM_ENDERTOOL));
        this.setTextureName(ReferenceTextures.getItemTextureName(ReferenceNames.NAME_ITEM_ENDERTOOL));
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        ToolType toolType = this.getToolType(stack);
        if (toolType != ToolType.INVALID)
        {
            return super.getUnlocalizedName() + "." + toolType.getName();
        }

        return super.getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items without NBT data: display the tips before the usual tooltip data
        if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().getBoolean("AddTooltips"))
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
    }

    @SideOnly(Side.CLIENT)
    public void addTooltips(ItemStack stack, List<String> list, boolean verbose)
    {
        ItemEnderUtilities.addTooltips(this.getUnlocalizedName(stack) + ".tooltips", list, verbose);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        ItemStack stack;
        if (Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            for (int i = 0; i <= 3; i++)
            {
                stack = new ItemStack(this, 1, 0);
                this.setToolType(stack, ToolType.valueOf(i));
                stack.getTagCompound().setBoolean("AddTooltips", true);
                list.add(stack);
            }
        }
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

    public ToolType getToolType(ItemStack stack)
    {
        if (stack == null)
        {
            return ToolType.INVALID;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
        {
            return ToolType.valueOfType(nbt.getString("ToolType"));
        }

        return ToolType.SHOVEL;
    }

    public boolean setToolType(ItemStack stack, ToolType type)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        nbt.setString("ToolType", type.getTypeString());

        return true;
    }

    public byte getToolModeByName(ItemStack stack, String name)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            return stack.getTagCompound().getByte(name);
        }

        return 0;
    }

    public void setToolModeByName(ItemStack stack, String name, byte value)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        nbt.setByte(name, value);
    }

    public String getToolClass(ItemStack stack)
    {
        //System.out.println("getToolClass()");
        if (stack != null)
        {
            ToolType type = this.getToolType(stack);
            if (type.equals(ToolType.INVALID) == false)
            {
                return type.getTypeString();
            }
        }

        return null;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack)
    {
        //System.out.println("getToolClasses()");
        String tc = this.getToolClass(stack);
        return tc != null ? ImmutableSet.of(tc) : super.getToolClasses(stack);
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
        //return this.func_150913_i().getMaxUses();
        return this.material.getMaxUses();
        //return 5;
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
    public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
    {
        //System.out.println("hitEntity(): living1: " + living1 + " living2: " + living2 + " remote: " + living2.worldObj.isRemote);
        if (stack == null || this.isToolBroken(stack) == true)
        {
            return false;
        }

        int amount = Math.min(2, this.getMaxDamage(stack) - stack.getItemDamage());
        stack.damageItem(amount, living2);

        // Tool just broke
        if (this.isToolBroken(stack) == true)
        {
            living1.renderBrokenItemStack(stack);
        }

        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase living)
    {
        //System.out.println("onBlockDestroyed(): living: " + living + " remote: " + living.worldObj.isRemote);

        // Don't use durability for breaking leaves with an axe
        if (block.getMaterial() != null && block.getMaterial() == Material.leaves && this.getToolType(stack).equals(ToolType.AXE))
        {
            return false;
        }

        // Don't use durability on instant-minable blocks (hardness == 0.0f), or if the tool is already broken
        if (this.isToolBroken(stack) == false && block.getBlockHardness(world, x, y, z) > 0.0f)
        {
            int dmg = 1;

            // Fast mode uses double the durability
            if (this.getToolModeByName(stack, "DigMode") == 1)
            {
                dmg++;
            }

            dmg = Math.min(dmg, this.getMaxDamage(stack) - stack.getItemDamage());
            stack.damageItem(dmg, living);

            // Tool just broke
            if (this.isToolBroken(stack) == true)
            {
                living.renderBrokenItemStack(stack);
            }

            return true;
        }

        return false;
    }

    public void handleHarvestDropsEvent(ItemStack toolStack, HarvestDropsEvent event)
    {
        if (event.world == null || event.world.isRemote == true)
        {
            return;
        }

        int numDropsOriginal = event.drops.size();
        byte mode = this.getToolModeByName(toolStack, "DropsMode");
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == 0)
        {
            return;
        }

        EntityPlayer player = event.harvester;
        boolean isSilk = event.isSilkTouching;

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == 1 && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 0)
        {
            Iterator<ItemStack> iter = event.drops.iterator();
            while (iter.hasNext() == true)
            {
                ItemStack stack = iter.next();
                if (stack != null && (isSilk || event.world.rand.nextFloat() < event.dropChance))
                {
                    if (player.inventory.addItemStackToInventory(stack.copy()) == true)
                    {
                        iter.remove();
                    }
                }
            }
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == 2 && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 1
                && UtilItemModular.useEnderCharge(toolStack, player, ENDER_CHARGE_COST, false) == true)
        {
            NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

            // For cross-dimensional item teleport we require the third tier of active Ender Core
            if (target == null || (target.dimension != player.dimension && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) < 2))
            {
                return;
            }

            World targetWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
            if (targetWorld == null)
            {
                return;
            }

            // Chunk load the target for 30 seconds
            ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension, target.posX >> 4, target.posZ >> 4, 30);

            // Block/inventory type link crystal
            if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
            {
                TileEntity te = targetWorld.getTileEntity(target.posX, target.posY, target.posZ);

                // Block has changed since binding, or does not implement IInventory, abort
                if (te == null || (te instanceof IInventory) == false || target.isTargetBlockUnchanged() == false)
                {
                    // Remove the bind
                    NBTHelperTarget.removeTargetTagFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);
                    player.addChatMessage(new ChatComponentTranslation("enderutilities.chat.message.enderbag.blockchanged"));
                    return;
                }

                if (te instanceof IInventory)
                {
                    Iterator<ItemStack> iter = event.drops.iterator();
                    while (iter.hasNext() == true)
                    {
                        ItemStack stack = iter.next();
                        if (stack != null && (isSilk || event.world.rand.nextFloat() < event.dropChance))
                        {
                            if (InventoryUtils.tryInsertItemStackToInventory((IInventory) te, stack.copy(), target.blockFace) == true)
                            {
                                iter.remove();
                            }
                        }
                    }
                }
            }
            // Location type Link Crystal, teleport/spawn the drops as EntityItems to the target spot
            else if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION)
            {
                Iterator<ItemStack> iter = event.drops.iterator();
                while (iter.hasNext() == true)
                {
                    ItemStack stack = iter.next();
                    if (stack != null && (isSilk || event.world.rand.nextFloat() < event.dropChance))
                    {
                        EntityItem entityItem = new EntityItem(targetWorld, target.dPosX, target.dPosY + 0.125d, target.dPosZ, stack.copy());
                        entityItem.motionX = entityItem.motionZ = 0.0d;
                        entityItem.motionY = 0.15d;

                        if (targetWorld.spawnEntityInWorld(entityItem) == true)
                        {
                            Particles.spawnParticles(targetWorld, "portal", target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
                            iter.remove();
                        }
                    }
                }
            }
        }

        // At least something got transported somewhere...
        if (event.drops.size() != numDropsOriginal)
        {
            // Transported the drops to somewhere remote
            if (mode == 2)
            {
                UtilItemModular.useEnderCharge(toolStack, player, ENDER_CHARGE_COST, true);
            }

            Sounds.playSound(event.world, event.x, event.y, event.z, "mob.endermen.portal", 0.08f, 1.8f);

            PacketHandler.INSTANCE.sendToAllAround(
                new MessageAddEffects(MessageAddEffects.EFFECT_TELEPORT, MessageAddEffects.PARTICLES,
                    event.x + 0.5d, event.y + 0.5d, event.z + 0.5d, 8, 0.3d, 0.4d),
                        new NetworkRegistry.TargetPoint(event.world.provider.dimensionId, event.x, event.y, event.z, 24.0d));
        }

        // All items successfully transported somewhere, cancel the drops
        if (event.drops.size() == 0)
        {
            event.dropChance = 0.0f;
        }
    }

    @Override
    public boolean func_150897_b(Block block)
    {
        //System.out.println("func_150897_b()");
        return false;
    }

    @Override
    public float func_150893_a(ItemStack stack, Block block)
    {
        //System.out.println("func_150893_a()");
        if (this.isToolBroken(stack) == true)
        {
            return 0.2f;
        }

        if (this.canHarvestBlock(block, stack) == true)
        {
            return this.efficiencyOnProperMaterial;
        }

        return 1.0f;
    }

    /**
     * ItemStack sensitive version of {@link #canHarvestBlock(Block)}
     * @param par1Block The block trying to harvest
     * @param itemStack The itemstack used to harvest the block
     * @return true if can harvest the block
     */
    @Override
    public boolean canHarvestBlock(Block block, ItemStack stack)
    {
        if (this.isToolBroken(stack) == true)
        {
            return false;
        }

        if (this.getToolType(stack).equals(ToolType.PICKAXE)) // Ender Pickaxe
        {
            if (block.getMaterial() == Material.rock
                || block.getMaterial() == Material.glass
                || block.getMaterial() == Material.ice
                || block.getMaterial() == Material.packedIce
                || block.getMaterial() == Material.piston
                || block.getMaterial() == Material.iron
                || block.getMaterial() == Material.anvil)
            {
                //System.out.println("canHarvestBlock(): true; Pickaxe");
                return true;
            }
        }
        else if (this.getToolType(stack).equals(ToolType.AXE)) // Ender Axe
        {
            if (block.getMaterial() == Material.wood
                || block.getMaterial() == Material.leaves
                || block.getMaterial() == Material.gourd
                || block.getMaterial() == Material.carpet
                || block.getMaterial() == Material.cloth
                || block.getMaterial() == Material.plants
                || block.getMaterial() == Material.vine)
            {
                //System.out.println("canHarvestBlock(): true; Axe");
                return true;
            }
        }
        else if (this.getToolType(stack).equals(ToolType.SHOVEL)) // Ender Shovel
        {
            if (block.getMaterial() == Material.ground
                || block.getMaterial() == Material.grass
                || block.getMaterial() == Material.sand
                || block.getMaterial() == Material.snow
                || block.getMaterial() == Material.craftedSnow
                || block.getMaterial() == Material.clay)
            {
                //System.out.println("canHarvestBlock(): true; Shovel");
                return true;
            }
        }

        //System.out.println("canHarvestBlock(): false");
        //return func_150897_b(block);
        return false;
    }

    /**
     * Metadata-sensitive version of getStrVsBlock
     * @param itemstack The Item Stack
     * @param block The block the item is trying to break
     * @param metadata The items current metadata
     * @return The damage strength
     */
    @Override
    public float getDigSpeed(ItemStack stack, Block block, int meta)
    {
        if (this.isToolBroken(stack) == true)
        {
            return 0.2f;
        }

        // Allow instant mine of leaves with the axe
        if (block.getMaterial() != null && block.getMaterial() == Material.leaves && this.getToolType(stack).equals(ToolType.AXE))
        {
            // This seems to be enough to instant mine leaves even when jumping/flying
            return 100.0f;
        }

        float eff = this.efficiencyOnProperMaterial;
        // 34 is the minimum to allow instant mining with just Efficiency V (= no beacon/haste) on cobble,
        // 124 is the minimum for iron blocks @ hardness 5.0f (which is about the highest of "normal" blocks), 1474 on obsidian.
        // So maybe around 160 might be ok? I don't want insta-mining on obsidian, but all other types of "rock".
        if (this.getToolModeByName(stack, "DigMode") == 1)
        {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) >= 5)
            {
                eff = 124.0f;
            }
            // This is enough to give instant mining for sandstone and netherrack without any Efficiency enchants.
            else
            {
                eff = 24.0f;
            }
        }

        if (ForgeHooks.isToolEffective(stack, block, meta))
        {
            //System.out.println("getDigSpeed(); isToolEffective() true: " + eff);
            return eff;
        }

        if (this.canHarvestBlock(block, stack))
        {
            //System.out.println("getDigSpeed(); canHarvestBlock() true: " + eff);
            return eff;
        }

        //System.out.println("getDigSpeed(); not effective: " + super.getDigSpeed(stack, block, meta));
        return super.getDigSpeed(stack, block, meta);
    }

    /**
     * Queries the harvest level of this item stack for the specified tool class,
     * Returns -1 if this tool is not of the specified type
     * 
     * @param stack This item stack instance
     * @param toolClass Tool Class
     * @return Harvest level, or -1 if not the specified tool type.
     */
    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass)
    {
        //System.out.println("getHarvestLevel(stack, \"" + toolClass + "\")");
        if (stack == null)
        {
            return -1;
        }

        if (this.isToolBroken(stack) == true)
        {
            return -1;
        }

        if (toolClass.equals(this.getToolClass(stack)) == true)
        {
            return this.func_150913_i().getHarvestLevel();
        }

        return -1;
    }

    /**
     * ItemStack sensitive version of getItemEnchantability
     * 
     * @param stack The ItemStack
     * @return the item echantability value
     */
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    /**
     * ItemStack sensitive version of getItemAttributeModifiers
     */
    @Override
    public Multimap getAttributeModifiers(ItemStack stack)
    {
        //System.out.println("getAttributeModifiers()");
        double dmg = this.damageVsEntity;

        // Broken tool
        if (this.isToolBroken(stack) == true)
        {
            dmg = 1.0d;
        }
        else
        {
            dmg += this.getToolType(stack).getAttackDamage();
        }

        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Tool modifier", dmg, 0));
        return multimap;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.parts = new String[] {"rod.1", "rod.2", "rod.3", "head.1", "head.2", "head.1.broken", "head.2.broken", "core.1", "core.2", "core.3",
                                    "capacitor.1", "capacitor.2", "capacitor.3", "linkcrystal.1", "linkcrystal.2"};

        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDER_PICKAXE + ".head.1");
        this.iconEmpty = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));
        this.iconArray = new IIcon[60];
        String prefix = this.getIconString() + ".";

        for (ToolType type : ToolType.values())
        {
            int id = type.getId();
            int start = id * this.parts.length;

            for (int j = 0; id >= 0 && j < this.parts.length && (start + j) < this.iconArray.length; j++)
            {
                this.iconArray[start + j] = iconRegister.registerIcon(prefix + type.getName() + "." + this.parts[j]);
            }
        }
    }

    /**
     * Render Pass sensitive version of hasEffect()
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack par1ItemStack, int pass)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderPasses(int metadata)
    {
        return 5;
    }

    /**
     * Return the correct icon for rendering based on the supplied ItemStack and render pass.
     *
     * Defers to {@link #getIconFromDamageForRenderPass(int, int)}
     * @param stack to render for
     * @param pass the multi-render pass
     * @return the icon
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        return this.getIcon(stack, renderPass, null, null, 0);
    }

    /**
     * Player, Render pass, and item usage sensitive version of getIconIndex.
     *
     * @param stack The item stack to get the icon for. (Usually this, and usingItem will be the same if usingItem is not null)
     * @param renderPass The pass to get the icon for, 0 is default.
     * @param player The player holding the item
     * @param usingItem The item the player is actively using. Can be null if not using anything.
     * @param useRemaining The ticks remaining for the active item.
     * @return The icon index
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        if (stack == null)
        {
            return this.itemIcon;
        }

        ToolType type = this.getToolType(stack);
        if (type.equals(ToolType.INVALID))
        {
            return this.itemIcon;
        }

        int i = type.getId() * this.parts.length;
        int tier = 0;

        switch(renderPass)
        {
            case 0: // 0: Rod
                i += getToolModeByName(stack, "DropsMode");
                break;
            case 1: // 1: Head
                i += getToolModeByName(stack, "DigMode") + 3; // Head icons start at index 3

                // Broken tool
                if (this.isToolBroken(stack) == true)
                {
                    i += 2;
                }
                break;
            case 2: // 2: Core
                tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
                if (tier >= 0)
                {
                    i += tier + 7;
                }
                else
                {
                    return this.iconEmpty;
                }
                break;
            case 3: // 3: Capacitor
                tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCAPACITOR);
                if (tier >= 0)
                {
                    i += tier + 10;
                }
                else
                {
                    return this.iconEmpty;
                }
                break;
            case 4: // 4: Link Crystal
                tier = this.getMaxModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL);
                if (tier >= 0)
                {
                    i += tier + 13;
                }
                else
                {
                    return this.iconEmpty;
                }
                break;
            default:
                return this.iconEmpty;
        }

        if (i < 0 || i >= this.iconArray.length)
        {
            return this.iconEmpty;
        }

        return this.iconArray[i];
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
            // Ctrl + Toggle mode: Toggle the block drops handling mode: normal, player, remote
            if (ReferenceKeys.keypressContainsControl(key))
            {
                byte mode = this.getToolModeByName(stack, "DropsMode");
                if (++mode > 2)
                {
                    mode = 0;
                }
                this.setToolModeByName(stack, "DropsMode", mode);
            }
            // Toggle the dig mode: normal, fast
            else
            {
                byte mode = this.getToolModeByName(stack, "DigMode");
                if (++mode > 1)
                {
                    mode = 0;
                }
                this.setToolModeByName(stack, "DigMode", mode);
            }
        }
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

    public enum ToolType
    {
        PICKAXE (0, "pickaxe",  ReferenceNames.NAME_ITEM_ENDER_PICKAXE, 2.0f),
        AXE     (1, "axe",      ReferenceNames.NAME_ITEM_ENDER_AXE,     3.0f),
        SHOVEL  (2, "shovel",   ReferenceNames.NAME_ITEM_ENDER_SHOVEL,  1.0f),
        HOE     (3, "hoe",      ReferenceNames.NAME_ITEM_ENDER_HOE,     0.0f),
        INVALID (-1, "null",    "null",                                 0.0f);

        private final int id;
        private final String typeString;
        private final String name;
        private final float attackDamage;

        private static final Map<Integer, ToolType> mapInt = new HashMap<Integer, ToolType>();
        private static final Map<String, ToolType> mapType = new HashMap<String, ToolType>();

        static
        {
            for (ToolType type : ToolType.values())
            {
                mapInt.put(type.getId(), type);
                mapType.put(type.getTypeString(), type);
            }
        }

        ToolType(int id, String type, String name, float attackDamage)
        {
            this.id = id;
            this.typeString = type;
            this.name = name;
            this.attackDamage = attackDamage;
        }

        public int getId()
        {
            return this.id;
        }

        public String getTypeString()
        {
            return this.typeString;
        }

        public String getName()
        {
            return this.name;
        }

        public float getAttackDamage()
        {
            return this.attackDamage;
        }

        public boolean equals(ToolType other)
        {
            return this.id == other.getId();
        }

        public static ToolType valueOf(int id)
        {
            ToolType type = mapInt.get(id);

            if (type != null)
            {
                return type;
            }

            return INVALID;
        }

        public static ToolType valueOfType(String typeName)
        {
            ToolType type = mapType.get(typeName);

            if (type != null)
            {
                return type;
            }

            return INVALID;
        }
    }
}
