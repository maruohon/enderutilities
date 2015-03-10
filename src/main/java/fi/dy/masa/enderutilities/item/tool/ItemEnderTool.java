package fi.dy.masa.enderutilities.item.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelRotation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelBlock;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelFactory;
import fi.dy.masa.enderutilities.client.resources.EnderUtilitiesModelRegistry;
import fi.dy.masa.enderutilities.client.resources.TextureItems;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceMaterial;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderTool extends ItemTool implements IKeyBound, IModular
{
    public static final int ENDER_CHARGE_COST = 50;
    public float efficiencyOnProperMaterial;
    public float damageVsEntity;
    private final Item.ToolMaterial material;

    /** Non-namespaced/non-mod-domain-prepended variant names for this item. */
    @SideOnly(Side.CLIENT)
    public String variants[];
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite textures[];
    @SideOnly(Side.CLIENT)
    public IFlexibleBakedModel models[];
    @SideOnly(Side.CLIENT)
    private String[] parts;

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

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote == true)
        {
            return true;
        }

        TileEntity te = world.getTileEntity(pos);
        // When sneak-right-clicking on an IInventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (player != null && player.isSneaking() == true && te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class)
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            UtilItemModular.setTarget(stack, player, pos, face, hitX, hitY, hitZ, false, false);
        }
        // Try to place a block from the slot right to the currently selected tool (or from slot 1 if tool is in slot 9)
        else if (player != null)
        {
            int origSlot = player.inventory.currentItem;
            int slot = (origSlot >= InventoryPlayer.getHotbarSize() - 1 ? 0 : origSlot + 1);
            ItemStack targetStack = player.inventory.getStackInSlot(slot);

            // If the tool is in the first slot of the hotbar and there is no ItemBlock in the second slot, we fall back to the last slot
            if (origSlot == 0 && (targetStack == null || (targetStack.getItem() instanceof ItemBlock) == false))
            {
                slot = InventoryPlayer.getHotbarSize() - 1;
                targetStack = player.inventory.getStackInSlot(slot);
            }

            // If the target stack is an ItemBlock, we try to place that in the world
            if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
            {
                player.inventory.currentItem = slot;
                targetStack.onItemUse(player, world, pos, face, hitX, hitY, hitZ);
                player.inventory.currentItem = origSlot;
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
            }
        }

        return true;
    }

    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
        int coreTier = this.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String preBlue = EnumChatFormatting.BLUE.toString();

        // Drops mode
        byte mode = this.getToolModeByName(stack, "DropsMode");
        String str = (mode == 0 ? "enderutilities.tooltip.item.normal" : mode == 1 ? "enderutilities.tooltip.item.endertool.playerinv" : "enderutilities.tooltip.item.endertool.remote");
        str = StatCollector.translateToLocal(str);
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.endertool.dropsmode") + ": " + preDGreen + str + rst);

        // Dig mode (normal/fast)
        mode = this.getToolModeByName(stack, "DigMode");
        str = (mode == 0 ? "enderutilities.tooltip.item.normal" : "enderutilities.tooltip.item.fast");
        str = StatCollector.translateToLocal(str);
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.endertool.digmode") + ": " + preDGreen + str + rst);

        // Installed Ender Core type
        str = StatCollector.translateToLocal("enderutilities.tooltip.item.endercore") + ": ";
        if (coreTier >= 0)
        {
            String coreType = (coreTier == 0 ? "enderutilities.tooltip.item.basic" : (coreTier == 1 ? "enderutilities.tooltip.item.enhanced" : "enderutilities.tooltip.item.advanced"));
            coreType = StatCollector.translateToLocal(coreType);
            str += preDGreen + coreType + rst + " (" + preBlue + StatCollector.translateToLocal("enderutilities.tooltip.item.tier") + " " + (coreTier + 1) + rst + ")";
        }
        else
        {
            String preRed = EnumChatFormatting.RED.toString();
            str += preRed + StatCollector.translateToLocal("enderutilities.tooltip.item.none") + rst;
        }
        list.add(str);

        // Link Crystals installed
        if (linkCrystalStack != null && linkCrystalStack.getItem() instanceof ItemLinkCrystal)
        {
            String preWhiteIta = EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString();
            // Valid target set in the currently selected Link Crystal
            if (NBTHelperTarget.itemHasTargetTag(linkCrystalStack) == true)
            {
                ((ItemLinkCrystal)linkCrystalStack.getItem()).addInformationSelective(linkCrystalStack, player, list, advancedTooltips, verbose);
            }
            else
            {
                list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.notargetset"));
            }

            int num = UtilItemModular.getModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
            int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
            String dName = (linkCrystalStack.hasDisplayName() ? preWhiteIta + linkCrystalStack.getDisplayName() + rst + " " : "");
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.selectedlinkcrystal.short") + String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.nolinkcrystals"));
        }

        // Capacitor installed
        if (capacitorStack != null && capacitorStack.getItem() instanceof ItemEnderCapacitor)
        {
            ((ItemEnderCapacitor)capacitorStack.getItem()).addInformationSelective(capacitorStack, player, list, advancedTooltips, verbose);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items "without" NBT data: display the tips before the usual tooltip data
        // We check for the ench and Items tags so that creative spawned items won't show the tooltip
        // once they have some other NBT data on them
        if (stack != null && stack.getTagCompound() != null && stack.getTagCompound().getBoolean("AddTooltips")
            && stack.getTagCompound().hasKey("ench") == false && stack.getTagCompound().hasKey("Items") == false)
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
            return;
        }

        tmpList.clear();
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
        }
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
        if (nbt != null && nbt.getString("ToolType").length() > 0)
        {
            return ToolType.valueOfType(nbt.getString("ToolType"));
        }
        else
        {
            this.setToolType(stack, ToolType.SHOVEL);
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
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, BlockPos pos, EntityLivingBase living)
    {
        //System.out.println("onBlockDestroyed(): living: " + living + " remote: " + living.worldObj.isRemote);

        // Don't use durability for breaking leaves with an axe
        if (block.getMaterial() != null && block.getMaterial() == Material.leaves && this.getToolType(stack).equals(ToolType.AXE))
        {
            return false;
        }

        // Don't use durability on instant-minable blocks (hardness == 0.0f), or if the tool is already broken
        if (this.isToolBroken(stack) == false && block.getBlockHardness(world, pos) > 0.0f)
        {
            int dmg = 1;

            // Fast mode uses double the durability
            if (this.getToolModeByName(stack, "DigMode") == 1)
            {
                dmg = 2;
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

        byte mode = this.getToolModeByName(toolStack, "DropsMode");
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == 0)
        {
            return;
        }

        EntityPlayer player = event.harvester;
        boolean isSilk = event.isSilkTouching;
        int numDropsOriginal = event.drops.size();

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

            // Bound to a vanilla Ender Chest
            if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK
                && target != null && "minecraft:ender_chest".equals(target.blockName) == true)
            {
                Iterator<ItemStack> iter = event.drops.iterator();
                while (iter.hasNext() == true)
                {
                    ItemStack stack = iter.next();
                    if (stack != null && (isSilk || event.world.rand.nextFloat() < event.dropChance))
                    {
                        if (InventoryUtils.tryInsertItemStackToInventory(player.getInventoryEnderChest(), stack.copy(), target.facing) == true)
                        {
                            iter.remove();
                        }
                    }
                }
            }
            // Bound to regular inventories
            else
            {
                // For cross-dimensional item teleport we require the third tier of active Ender Core
                if (NBTHelperPlayer.canAccessSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL, player) == false
                    || target == null || (target.dimension != player.dimension && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) < 2))
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
                    TileEntity te = targetWorld.getTileEntity(target.pos);

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
                                if (InventoryUtils.tryInsertItemStackToInventory((IInventory) te, stack.copy(), target.facing) == true)
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
                                Particles.spawnParticles(targetWorld, EnumParticleTypes.PORTAL, target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
                                iter.remove();
                            }
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

            double x = event.pos.getX();
            double y = event.pos.getY();
            double z = event.pos.getZ();
            PacketHandler.INSTANCE.sendToAllAround(
                new MessageAddEffects(MessageAddEffects.EFFECT_ENDER_TOOLS, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND,
                x + 0.5d, y + 0.5d, z + 0.5d, 8, 0.2d, 0.3d), new NetworkRegistry.TargetPoint(event.world.provider.getDimensionId(), x, y, z, 24.0d));
        }

        // All items successfully transported somewhere, cancel the drops
        if (event.drops.size() == 0)
        {
            event.dropChance = 0.0f;
        }
    }

    @Override
    public float getStrVsBlock(ItemStack stack, Block block)
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

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return false;
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
    public float getDigSpeed(ItemStack stack, IBlockState iBlockState)
    {
        if (this.isToolBroken(stack) == true)
        {
            return 0.2f;
        }

        Block block = iBlockState.getBlock();
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

        //if (ForgeHooks.isToolEffective(stack) == true) // FIXME 1.8 update wtf, how can we use this anymore without world and BlockPos?
        if (block.isToolEffective(this.getToolClass(stack), iBlockState) == true)
        {
            //System.out.println("getDigSpeed(); isToolEffective() true: " + eff);
            return eff;
        }

        if (this.canHarvestBlock(block, stack) == true)
        {
            //System.out.println("getDigSpeed(); canHarvestBlock() true: " + eff);
            return eff;
        }

        //System.out.println("getDigSpeed(); not effective: " + super.getDigSpeed(stack, block, meta));
        return super.getDigSpeed(stack, iBlockState);
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
            return this.getToolMaterial().getHarvestLevel();
        }

        return -1;
    }

    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

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
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(Item.itemModifierUUID, "Tool modifier", dmg, 0));
        return multimap;
    }

    public void changeDigMode(ItemStack stack)
    {
        byte mode = this.getToolModeByName(stack, "DigMode");
        if (++mode > 1)
        {
            mode = 0;
        }
        this.setToolModeByName(stack, "DigMode", mode);
    }

    public void changeDropsMode(ItemStack stack)
    {
        byte mode = this.getToolModeByName(stack, "DropsMode");
        if (++mode > 2)
        {
            mode = 0;
        }
        this.setToolModeByName(stack, "DropsMode", mode);
    }

    public void changePrivacyMode(ItemStack stack, EntityPlayer player)
    {
        NBTHelperPlayer data = NBTHelperPlayer.getPlayerDataFromSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (data != null && data.isOwner(player) == true)
        {
            data.isPublic = ! data.isPublic;
            data.writeToSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL);
        }
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        if (stack == null || ReferenceKeys.getBaseKey(key) != ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            return;
        }

        // Just Toggle mode key: Change the dig mode
        if (key == ReferenceKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.changeDigMode(stack);
        }
        // Shift + (Ctrl + ) Toggle mode
        else if (ReferenceKeys.keypressContainsShift(key) == true && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressContainsControl(key));
        }
        // Shift + Alt + Toggle mode: Store the player's current location
        else if (ReferenceKeys.keypressContainsShift(key) == true
                && ReferenceKeys.keypressContainsAlt(key) == true
                && ReferenceKeys.keypressContainsControl(key) == false)
        {
            UtilItemModular.setTarget(stack, player, true);
        }
        // Ctrl + Toggle mode: Toggle the block drops handling mode: normal, player, remote
        else if (ReferenceKeys.keypressContainsControl(key) == true
                && ReferenceKeys.keypressContainsShift(key) == false
                && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeDropsMode(stack);
        }
        // Alt + Toggle mode: Toggle the private/public mode
        else if (ReferenceKeys.keypressContainsAlt(key) == true
                && ReferenceKeys.keypressContainsShift(key) == false
                && ReferenceKeys.keypressContainsControl(key) == false)
        {
            this.changePrivacyMode(stack, player);
        }
    }

    @Override
    public int getModuleCount(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getModuleCount(stack, moduleType);
    }

    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 5;
    }

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
            return 3;
        }

        return 0;
    }

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

    @Override
    public int getMaxModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(stack, moduleType);
    }

    public int getSelectedModuleTier(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(stack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack stack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(stack, moduleType);
    }

    @Override
    public ItemStack setSelectedModuleStack(ItemStack toolStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(toolStack, moduleType, moduleStack);
    }

    @Override
    public ItemStack changeSelectedModule(ItemStack stack, ModuleType moduleType, boolean reverse)
    {
        return UtilItemModular.changeSelectedModule(stack, moduleType, reverse);
    }

    @Override
    public List<NBTTagCompound> getAllModules(ItemStack stack)
    {
        return UtilItemModular.getAllModules(stack);
    }

    @Override
    public ItemStack setAllModules(ItemStack stack, List<NBTTagCompound> modules)
    {
        return UtilItemModular.setAllModules(stack, modules);
    }

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

    /*
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

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDER_PICKAXE + ".head.1");
        this.iconEmpty = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));
        this.parts = new String[] {"rod.1", "head.1", "head.2", "head.3",
                                            "head.1.glow", "head.2.glow", "head.3.glow",
                                            "head.1.broken", "head.2.broken", "head.3.broken",
                                            "head.1.glow.broken", "head.2.glow.broken", "head.3.glow.broken",
                                            "core.1", "core.2", "core.3",
                                            "capacitor.1", "capacitor.2", "capacitor.3",
                                            "linkcrystal.1", "linkcrystal.2"};

        this.iconArray = new IIcon[this.parts.length * 4];
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

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        return this.getIcon(stack, renderPass, null, null, 0);
    }

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
                break;
            case 1: // 1: Head
                // The head color is defined by the drops handling mode
                i += getToolModeByName(stack, "DropsMode") + 1; // Head icons start at index 1

                // Fast mode uses the glow variation of the head
                if (getToolModeByName(stack, "DigMode") != 0)
                {
                    i += 3;
                }

                // Broken tool
                if (this.isToolBroken(stack) == true)
                {
                    i += 6;
                }
                break;
            case 2: // 2: Core
                tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
                if (tier >= 0)
                {
                    i += tier + 13;
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
                    i += tier + 16;
                }
                else
                {
                    return this.iconEmpty;
                }
                break;
            case 4: // 4: Link Crystal
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
                    i += tier + 19;
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
    */

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
            textureMap.setTextureEntry(name, new TextureItems(name));
            this.textures[i] = textureMap.getTextureExtry(name);
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerModels(IRegistry modelRegistry, ItemModelMesher itemModelMesher, TextureMap textureMap, Map<ResourceLocation, ModelBlock> modelMap)
    {
        itemModelMesher.register(this, EnderUtilitiesModelRegistry.baseItemMeshDefinition);
        ItemModelGenerator itemModelGenerator = new ItemModelGenerator();

        int len = this.variants.length;
        this.models = new IFlexibleBakedModel[len];

        ModelBlock base = EnderUtilitiesModelRegistry.modelBlockBase;
        for (int i = 0; i < len; ++i)
        {
            String modelName = Reference.MOD_ID + ":models/item/" + this.variants[i];
            ModelBlock modelBlock = EnderUtilitiesModelBlock.createNewModelBlockForTexture(base, this.textures[i], modelName, modelMap);
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
         this.parts = new String[] {"rod.1", "head.1", "head.2", "head.3",
                "head.1.glow", "head.2.glow", "head.3.glow",
                "head.1.broken", "head.2.broken", "head.3.broken",
                "head.1.glow.broken", "head.2.glow.broken", "head.3.glow.broken",
                "core.1", "core.2", "core.3",
                "capacitor.1", "capacitor.2", "capacitor.3",
                "linkcrystal.1", "linkcrystal.2"};

         int partsLen = this.parts.length;
         int variantsLen = partsLen * 4;
         String prefix = ReferenceNames.NAME_ITEM_ENDERTOOL + ".";
         String[] allVariants = new String[variantsLen];

         for (ToolType type : ToolType.values())
         {
             int id = type.getId();
             int start = id * partsLen;

             for (int j = 0; id >= 0 && j < partsLen; j++)
             {
                 allVariants[start + j] = prefix + type.getName() + "." + this.parts[j];
             }
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

        ToolType type = this.getToolType(stack);
        if (type.equals(ToolType.INVALID))
        {
            return this.models[0];
        }

        int i = type.getId() * this.parts.length;
        //int tier = 0;

        // TODO: How does one do multi layer textures in 1.8 ???
        i += getToolModeByName(stack, "DropsMode") + 1; // Head icons start at index 1

        // Fast mode uses the glow variation of the head
        if (getToolModeByName(stack, "DigMode") != 0)
        {
            i += 3;
        }

        // Broken tool
        if (this.isToolBroken(stack) == true)
        {
            i += 6;
        }

        if (i >= this.textures.length)
        {
            return this.models[0];
        }

        return this.models[i];
    }
}
