package fi.dy.masa.enderutilities.item.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import fi.dy.masa.enderutilities.config.Configs;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.item.base.IAnvilRepairable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.registry.EnderUtilitiesItems;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderTool extends ItemLocationBoundModular implements IAnvilRepairable
{
    public static final Item.ToolMaterial ENDER_ALLOY_ADVANCED =
        EnumHelper.addToolMaterial(ReferenceNames.NAME_MATERIAL_ENDERALLOY_ADVANCED,
            Configs.harvestLevelEnderAlloyAdvanced, 3120, 12.0f, 0.0f, 15);

    public static final int ENDER_CHARGE_COST = 50;
    public float efficiencyOnProperMaterial;
    private final Item.ToolMaterial material;

    static
    {
        ENDER_ALLOY_ADVANCED.setRepairItem(new ItemStack(EnderUtilitiesItems.enderPart, 1, 2));
    }

    public ItemEnderTool()
    {
        super();
        this.material = ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setNoRepair();
        this.efficiencyOnProperMaterial = this.material.getEfficiencyOnProperMaterial();
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERTOOL);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        ToolType toolType = ToolType.fromStack(stack);
        if (toolType != ToolType.INVALID)
        {
            return super.getUnlocalizedName() + "_" + toolType.getName();
        }

        return super.getUnlocalizedName();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
            EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = player.getHeldItem(hand);
        TileEntity te = world.getTileEntity(pos);

        // When sneak-right-clicking on an inventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (player != null && player.isSneaking() && te != null &&
            (te.getClass() == TileEntityEnderChest.class || te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            if (world.isRemote == false)
            {
                UtilItemModular.setTarget(stack, player,pos, side, hitX, hitY, hitZ, false, false);
            }

            return EnumActionResult.SUCCESS;
        }
        // Hoe
        else if (ToolType.fromStack(stack).equals(ToolType.HOE))
        {
            if (world.isRemote)
            {
                return EnumActionResult.SUCCESS;
            }

            if (PowerStatus.fromStack(stack) == PowerStatus.POWERED)
            {
                // Didn't till any soil; try to plant stuff from the player inventory or a remote inventory
                if (this.useHoeArea(stack, player, world, pos, side, 1, 1) == false)
                {
                    this.useHoeToPlantArea(stack, player, world, pos, side, hitX, hitY, hitZ, 1, 1);
                }
            }
            else
            {
                // Didn't till any soil; try to plant stuff from the player inventory or a remote inventory
                if (this.useHoe(stack, player, world, pos, side) == false)
                {
                    this.useHoeToPlant(stack, player, world, pos, side, hitX, hitY, hitZ);
                }
            }
        }
        // Try to place a block from the slot right to the currently selected tool (or from slot 1 if tool is in slot 9)
        else if (player != null)
        {
            if (world.isRemote == false)
            {
                return this.placeBlock(stack, player, world, pos, side, hitX, hitY, hitZ);
            }

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    private EnumActionResult placeBlock(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int origSlot = playerIn.inventory.currentItem;
        int slot = -1;

        ItemStack targetStack = playerIn.getHeldItemOffhand();
        if (targetStack == null || (targetStack.getItem() instanceof ItemBlock) == false)
        {
            slot = (origSlot >= InventoryPlayer.getHotbarSize() - 1 ? 0 : origSlot + 1);
            targetStack = playerIn.inventory.getStackInSlot(slot);

            // If the tool is in the first slot of the hotbar and there is no ItemBlock in the second slot, we fall back to the last slot
            if (origSlot == 0 && (targetStack == null || (targetStack.getItem() instanceof ItemBlock) == false))
            {
                slot = InventoryPlayer.getHotbarSize() - 1;
                targetStack = playerIn.inventory.getStackInSlot(slot);
            }
        }

        // If the target stack is an ItemBlock, we try to place that in the world
        if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
        {
            // Check if we can place the block
            if (BlockUtils.checkCanPlaceBlockAt(worldIn, pos, side, ((ItemBlock)targetStack.getItem()).block))
            {
                EnumActionResult result;
                // Off-hand
                if (slot == -1)
                {
                    result = ForgeHooks.onPlaceItemIntoWorld(targetStack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, EnumHand.OFF_HAND);
                    if (targetStack.stackSize <= 0)
                    {
                        playerIn.setHeldItem(EnumHand.OFF_HAND, null);
                    }
                }
                else
                {
                    playerIn.inventory.currentItem = slot;
                    result = ForgeHooks.onPlaceItemIntoWorld(targetStack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, EnumHand.MAIN_HAND);
                    if (targetStack.stackSize <= 0)
                    {
                        playerIn.inventory.setInventorySlotContents(slot, null);
                    }
                    playerIn.inventory.currentItem = origSlot;
                }

                if (result == EnumActionResult.SUCCESS)
                {
                    SoundEvent sound = ((ItemBlock)targetStack.getItem()).block.getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn).getPlaceSound();
                    worldIn.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
                }

                playerIn.inventory.markDirty();
                playerIn.inventoryContainer.detectAndSendChanges();

                return result;
            }
        }

        return EnumActionResult.PASS;
    }

    public boolean useHoeArea(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, int rWidth, int rHeight)
    {
        boolean northSouth = (((int)MathHelper.floor(player.rotationYaw * 4.0f / 360.0f + 0.5f)) & 1) == 0;
        boolean retValue = false;

        if (northSouth == false)
        {
            int tmp = rWidth;
            rWidth = rHeight;
            rHeight = tmp;
        }

        for (int x = pos.getX() - rWidth; x <= (pos.getX() + rWidth); ++x)
        {
            for (int z = pos.getZ() - rHeight; z <= (pos.getZ() + rHeight); ++z)
            {
                retValue |= this.useHoe(stack, player, world, new BlockPos(x, pos.getY(), z), side);
            }
        }

        return retValue;
    }

    public boolean useHoe(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side)
    {
        if (player.canPlayerEdit(pos, side, stack) == false)
        {
            return false;
        }

        int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(stack, player, world, pos);
        if (hook != 0)
        {
            return hook > 0;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (side != EnumFacing.DOWN && world.isAirBlock(pos.up()))
        {
            IBlockState newBlockState = null;

            if (block == Blocks.GRASS)
            {
                newBlockState = Blocks.FARMLAND.getDefaultState();
            }
            else if (block == Blocks.DIRT)
            {
                if (state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT)
                {
                    newBlockState = Blocks.FARMLAND.getDefaultState();
                }
                else if (state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT)
                {
                    newBlockState = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT);
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }

            world.playSound(null, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
                    SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0f, 1.0f);

            if (world.isRemote == false)
            {
                world.setBlockState(pos, newBlockState);
                // 0.4.2: No idea why this is needed to get the blocks to update to the client, as it should be called from setBlock() already...
                world.notifyBlockUpdate(pos, state, state, 3);
                this.addToolDamage(stack, 1, player, player);
            }

            return true;
        }

        return false;
    }

    public boolean useHoeToPlantArea(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int rWidth, int rHeight)
    {
        boolean northSouth = (((int)MathHelper.floor(player.rotationYaw * 4.0f / 360.0f + 0.5f)) & 1) == 0;
        boolean retValue = false;

        if (northSouth == false)
        {
            int tmp = rWidth;
            rWidth = rHeight;
            rHeight = tmp;
        }

        for (int x = pos.getX() - rWidth; x <= (pos.getX() + rWidth); ++x)
        {
            for (int z = pos.getZ() - rHeight; z <= (pos.getZ() + rHeight); ++z)
            {
                retValue |= this.useHoeToPlant(stack, player, world, new BlockPos(x, pos.getY(), z), side, hitX, hitY, hitZ);
            }
        }

        return retValue;
    }

    public boolean useHoeToPlant(ItemStack toolStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true) == false)
        {
            return false;
        }

        IItemHandler inv = this.getLinkedInventoryWithChecks(toolStack, player);

        if (inv != null)
        {
            for (int slot = 0; slot < inv.getSlots(); slot++)
            {
                if (this.plantItemFromInventorySlot(world, player, inv, slot, pos, side, hitX, hitY, hitZ))
                {
                    // Use Ender Charge if planting from a remote inventory
                    if (DropsMode.fromStack(toolStack) == DropsMode.REMOTE)
                    {
                        UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false);
                    }

                    Effects.addItemTeleportEffects(world, pos);

                    return true;
                }
            }
        }

        return false;
    }

    private boolean plantItemFromInventorySlot(World world, EntityPlayer player, IItemHandler inv, int slot, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        boolean ret = false;
        ItemStack plantStack = inv.getStackInSlot(slot);

        if (plantStack != null && plantStack.getItem() instanceof IPlantable)
        {
            plantStack = inv.extractItem(slot, 1, false);
            if (plantStack == null)
            {
                return false;
            }

            if (plantStack.onItemUse(player, world, pos, EnumHand.MAIN_HAND, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS)
            {
                ret = true;
            }

            if (plantStack.stackSize > 0)
            {
                inv.insertItem(slot, plantStack, false);
            }

            if (inv instanceof PlayerMainInvWrapper && player instanceof EntityPlayerMP)
            {
                player.inventoryContainer.detectAndSendChanges();
            }
        }

        return ret;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack)
    {
        return newStack.getItem() != oldStack.getItem() || newStack.getMetadata() != oldStack.getMetadata();
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return true;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack)
    {
        String tc = ToolType.fromStack(stack).getToolClass();
        return tc != null ? ImmutableSet.of(tc) : super.getToolClasses(stack);
    }

    @Override
    public boolean isDamageable()
    {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack)
    {
        return this.material.getMaxUses();
    }

    @Override
    public boolean isDamaged(ItemStack stack)
    {
        return this.getDamage(stack) > 0;
    }

    @Override
    public int getDamage(ItemStack stack)
    {
        return NBTUtils.getShort(stack, null, "ToolDamage");
    }

    @Override
    public void setDamage(ItemStack stack, int damage)
    {
        damage = MathHelper.clamp(damage, 0, this.material.getMaxUses());
        NBTUtils.setShort(stack, null, "ToolDamage", (short)damage);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        return this.isDamaged(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        return (double)this.getDamage(stack) / (double)this.getMaxDamage(stack);
    }

    public boolean isToolBroken(ItemStack stack)
    {
        return NBTUtils.getShort(stack, null, "ToolDamage") >= this.material.getMaxUses();
    }

    public boolean addToolDamage(ItemStack stack, int amount, EntityLivingBase living1, EntityLivingBase living2)
    {
        //System.out.println("addToolDamage(): living1: " + living1 + " living2: " + living2 + " remote: " + living2.worldObj.isRemote);
        if (stack == null || this.isToolBroken(stack))
        {
            return false;
        }

        if (amount > 0)
        {
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("unbreaking"), stack);
            int amountNegated = 0;

            for (int i = 0; unbreakingLevel > 0 && i < amount; i++)
            {
                if (itemRand.nextInt(amount + 1) > 0)
                {
                    amountNegated++;
                }
            }

            amount -= amountNegated;

            if (amount <= 0)
            {
                return false;
            }
        }

        int damage = this.getDamage(stack);
        damage = Math.min(damage + amount, this.material.getMaxUses());
        this.setDamage(stack, damage);

        // Tool just broke
        if (damage == this.material.getMaxUses())
        {
            living1.renderBrokenItemStack(stack);
        }

        return true;
    }

    @Override
    public boolean repairItem(ItemStack stack, int amount)
    {
        if (amount == -1)
        {
            amount = this.material.getMaxUses();
        }

        int damage = Math.max(this.getDamage(stack) - amount, 0);
        boolean repaired = damage != this.getDamage(stack);

        this.setDamage(stack, damage);

        return repaired;
    }

    @Override
    public boolean isRepairItem(@Nonnull ItemStack stackTool, @Nonnull ItemStack stackMaterial)
    {
        return InventoryUtils.areItemStacksEqual(stackMaterial, this.material.getRepairItemStack());
    }

    @Override
    public boolean canApplyEnchantment(ItemStack stackTool, Enchantment enchantment)
    {
        if (enchantment.type == EnumEnchantmentType.ALL ||
            enchantment.type == EnumEnchantmentType.BREAKABLE)
        {
            return true;
        }

        switch (ToolType.fromStack(stackTool))
        {
            case SHOVEL:
            case PICKAXE:
                return enchantment.type == EnumEnchantmentType.DIGGER;

            case AXE:
                return enchantment.type == EnumEnchantmentType.WEAPON ||
                       enchantment.type == EnumEnchantmentType.DIGGER;

            case HOE:
            default:
                return false;
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
    {
        this.addToolDamage(stack, 2, living1, living2);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase livingBase)
    {
        //System.out.println("onBlockDestroyed(): living: " + living + " remote: " + living.worldObj.isRemote);

        if ((livingBase instanceof EntityPlayer) && this.isCreativeLikeBreakingEnabled(stack))
        {
            ((EntityPlayer) livingBase).getCooldownTracker().setCooldown(this, 6);
        }

        // Don't use durability for breaking leaves with an axe
        if (state.getMaterial() == Material.LEAVES && ToolType.fromStack(stack).equals(ToolType.AXE))
        {
            return false;
        }

        // Don't use durability on instant-minable blocks (hardness == 0.0f), or if the tool is already broken
        if (this.isToolBroken(stack) == false && state.getBlockHardness(world, pos) > 0.0f)
        {
            // Fast mode uses double the durability, but not while using the Creative Breaking upgrade
            int dmg = (PowerStatus.fromStack(stack) == PowerStatus.POWERED && this.isCreativeLikeBreakingEnabled(stack) == false ? 2 : 1);
            this.addToolDamage(stack, dmg, livingBase, livingBase);
            return true;
        }

        return false;
    }

    private IItemHandler getLinkedInventoryWithChecks(ItemStack toolStack, EntityPlayer player)
    {
        DropsMode mode = DropsMode.fromStack(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == DropsMode.NORMAL)
        {
            return null;
        }

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == DropsMode.PLAYER && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC)
        {
            return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP); // main inventory
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == DropsMode.REMOTE &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ENHANCED &&
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true))
        {
            return UtilItemModular.getBoundInventory(toolStack, player, 15);
        }

        return null;
    }

    public void handleHarvestDropsEvent(ItemStack toolStack, HarvestDropsEvent event)
    {
        if (this.isToolBroken(toolStack) || event.getWorld() == null || event.getWorld().isRemote)
        {
            return;
        }

        DropsMode mode = DropsMode.fromStack(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == DropsMode.NORMAL)
        {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        EntityPlayer player = event.getHarvester();
        boolean isSilk = event.isSilkTouching();
        boolean transported = false;

        // Don't try to handle the drops via other means in the Remote mode until after we try to transport them here first
        if (mode == DropsMode.PLAYER &&
            this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC &&
            MinecraftForge.EVENT_BUS.post(new PlayerItemPickupEvent(player, drops)))
        {
            Effects.addItemTeleportEffects(event.getWorld(), event.getPos());
            return;
        }

        IItemHandler inv = this.getLinkedInventoryWithChecks(toolStack, player);
        if (inv != null)
        {
            Iterator<ItemStack> iter = drops.iterator();

            while (iter.hasNext())
            {
                ItemStack stack = iter.next();
                if (stack != null && (isSilk || event.getWorld().rand.nextFloat() < event.getDropChance()))
                {
                    ItemStack stackTmp = InventoryUtils.tryInsertItemStackToInventory(inv, stack.copy());
                    if (stackTmp == null)
                    {
                        iter.remove();
                        transported = true;
                    }
                    else if (stackTmp.stackSize != stack.stackSize)
                    {
                        stack.stackSize = stackTmp.stackSize;
                        transported = true;
                    }
                }
            }
        }
        // Location type Link Crystal, teleport/spawn the drops as EntityItems to the target spot
        else if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION)
        {
            TargetData target = TargetData.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

            // For cross-dimensional item teleport we require the third tier of active Ender Core
            if (OwnerData.canAccessSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL, player) == false
                || (target.dimension != player.getEntityWorld().provider.getDimension() &&
                    this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) != ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED))
            {
                return;
            }

            World targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(target.dimension);
            if (targetWorld == null)
            {
                return;
            }

            // Chunk load the target for 30 seconds
            ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4, 30);

            Iterator<ItemStack> iter = drops.iterator();
            while (iter.hasNext())
            {
                ItemStack stack = iter.next();
                if (stack != null && (isSilk || event.getWorld().rand.nextFloat() < event.getDropChance()))
                {
                    EntityItem entityItem = new EntityItem(targetWorld, target.dPosX, target.dPosY + 0.125d, target.dPosZ, stack.copy());
                    entityItem.motionX = entityItem.motionZ = 0.0d;
                    entityItem.motionY = 0.15d;

                    if (targetWorld.spawnEntity(entityItem))
                    {
                        Effects.spawnParticles(targetWorld, EnumParticleTypes.PORTAL, target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
                        iter.remove();
                        transported = true;
                    }
                }
            }
        }

        // At least something got transported somewhere...
        if (transported)
        {
            // Transported the drops to somewhere remote
            if (mode == DropsMode.REMOTE)
            {
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false);
            }

            Effects.addItemTeleportEffects(event.getWorld(), event.getPos());
        }

        // If we failed to handle the drops ourselves, then try to handle them via other means
        if (drops.size() > 0 &&
            this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC &&
            MinecraftForge.EVENT_BUS.post(new PlayerItemPickupEvent(player, drops)))
        {
            Effects.addItemTeleportEffects(event.getWorld(), event.getPos());
        }

        // All items successfully transported somewhere, cancel the drops
        if (drops.size() == 0)
        {
            event.setDropChance(0.0f);
        }
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        if (this.isToolBroken(stack))
        {
            return 0.2f;
        }

        ToolType tool = ToolType.fromStack(stack);

        if (this.isCreativeLikeBreakingEnabled(stack) &&  this.canHarvestBlock(state, stack))
        {
            return 1600f;
        }

        // Allow instant mine of leaves with the axe
        if (state.getMaterial() == Material.LEAVES && tool.equals(ToolType.AXE))
        {
            // This seems to be enough to instant mine leaves even when jumping/flying
            return 100.0f;
        }

        float eff = this.efficiencyOnProperMaterial;
        // 34 is the minimum to allow instant mining with just Efficiency V (= no beacon/haste) on cobble,
        // 124 is the minimum for iron blocks @ hardness 5.0f (which is about the highest of "normal" blocks), 1474 on obsidian.
        // So maybe around 160 might be ok? I don't want insta-mining on obsidian, but all other types of "rock".
        if (PowerStatus.fromStack(stack) == PowerStatus.POWERED)
        {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.getEnchantmentByLocation("efficiency"), stack) >= 5)
            {
                eff = 124.0f;
            }
            // This is enough to give instant mining for sandstone and netherrack without any Efficiency enchants.
            else
            {
                eff = 24.0f;
            }
        }

        //if (ForgeHooks.isToolEffective(stack, block, meta))
        if (state.getBlock().isToolEffective(tool.getToolClass(), state))
        {
            //System.out.println("getStrVsBlock(); isToolEffective() true: " + eff);
            return eff;
        }

        if (this.canHarvestBlock(state, stack))
        {
            //System.out.println("getStrVsBlock(); canHarvestBlock() true: " + eff);
            return eff;
        }

        //System.out.println("getStrVsBlock(); not effective: " + super.getStrVsBlock(stack, block, meta));
        return super.getStrVsBlock(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack)
    {
        if (this.isToolBroken(stack))
        {
            return false;
        }

        ToolType tool = ToolType.fromStack(stack);

        if (tool.equals(ToolType.PICKAXE)) // Ender Pickaxe
        {
            if (state.getMaterial() == Material.ROCK
                || state.getMaterial() == Material.GLASS
                || state.getMaterial() == Material.ICE
                || state.getMaterial() == Material.PACKED_ICE
                || state.getMaterial() == Material.REDSTONE_LIGHT
                || state.getMaterial() == Material.PISTON
                || state.getMaterial() == Material.IRON
                || state.getMaterial() == Material.ANVIL)
            {
                //System.out.println("canHarvestBlock(): true; Pickaxe");
                return true;
            }
        }
        else if (tool.equals(ToolType.AXE)) // Ender Axe
        {
            if (state.getMaterial() == Material.WOOD
                || state.getMaterial() == Material.LEAVES
                || state.getMaterial() == Material.GOURD
                || state.getMaterial() == Material.CARPET
                || state.getMaterial() == Material.CLOTH
                || state.getMaterial() == Material.PLANTS
                || state.getMaterial() == Material.VINE)
            {
                //System.out.println("canHarvestBlock(): true; Axe");
                return true;
            }
        }
        else if (tool.equals(ToolType.SHOVEL)) // Ender Shovel
        {
            if (state.getMaterial() == Material.GROUND
                || state.getMaterial() == Material.GRASS
                || state.getMaterial() == Material.SAND
                || state.getMaterial() == Material.SNOW
                || state.getMaterial() == Material.CRAFTED_SNOW
                || state.getMaterial() == Material.CLAY)
            {
                //System.out.println("canHarvestBlock(): true; Shovel");
                return true;
            }
        }

        //System.out.println("canHarvestBlock(): false");
        return false;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
    {
        //System.out.println("getHarvestLevel(stack, \"" + toolClass + "\")");
        if (stack != null && this.isToolBroken(stack) == false && toolClass.equals(ToolType.fromStack(stack).getToolClass()))
        {
            return this.material.getHarvestLevel();
        }

        return -1;
    }

    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot, stack);
        //System.out.println("getAttributeModifiers()");
        double dmg = 0.5f; // Default to almost no damage if the tool is broken

        ToolType toolType = ToolType.fromStack(stack);
        // Broken not tool
        if (this.isToolBroken(stack) == false)
        {
            dmg = toolType.getAttackDamage();
        }

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            String modifierName = toolType == ToolType.HOE ? "Weapon modifier" : "Tool modifier";

            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, modifierName, dmg, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, modifierName, toolType.getAttackSpeed(), 0));
        }

        return multimap;
    }

    public void cyclePoweredMode(ItemStack stack)
    {
        NBTUtils.cycleByteValue(stack, null, "Powered", 1);
    }

    public void cycleCreativeBreakingMode(ItemStack stack)
    {
        if (this.getInstalledModuleCount(stack, ModuleType.CREATIVE_BREAKING) > 0)
        {
            NBTUtils.cycleByteValue(stack, null, "CreativeBreaking", 1);
        }
        else
        {
            NBTUtils.setByte(stack, null, "CreativeBreaking", (byte) 0);
        }
    }

    public void cycleDropsMode(ItemStack stack)
    {
        NBTUtils.cycleByteValue(stack, null, "DropsMode", 2);
    }

    public boolean isCreativeLikeBreakingEnabled(ItemStack stack)
    {
        return this.getInstalledModuleCount(stack, ModuleType.CREATIVE_BREAKING) > 0 &&
               CreativeBreaking.fromStack(stack) == CreativeBreaking.ENABLED;
    }
    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode key: Change the dig mode
        if (key == HotKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            this.cyclePoweredMode(stack);
        }
        // Shift + Toggle mode: Toggle the block drops handling mode: normal, player, remote
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            this.cycleDropsMode(stack);
        }
        // Ctrl + Shift + Toggle mode: Toggle the creative-like breaking mode on/off
        else if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT_CTRL))
        {
            this.cycleCreativeBreakingMode(stack);
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
        }
    }

    @Override
    public int getMaxModules(ItemStack containerStack)
    {
        return 6;
    }

    @Override
    public int getMaxModules(ItemStack containerStack, ModuleType moduleType)
    {
        if (moduleType.equals(ModuleType.TYPE_ENDERCORE))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.TYPE_ENDERCAPACITOR))
        {
            return 1;
        }

        if (moduleType.equals(ModuleType.CREATIVE_BREAKING))
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
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
    {
        if (moduleStack == null || (moduleStack.getItem() instanceof IModule) == false)
        {
            return 0;
        }

        IModule imodule = (IModule) moduleStack.getItem();
        ModuleType moduleType = imodule.getModuleType(moduleStack);
        int tier = imodule.getModuleTier(moduleStack);

        // Allow the in-world/location and block/inventory type Link Crystals
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) &&
            (tier != ItemLinkCrystal.TYPE_LOCATION && tier != ItemLinkCrystal.TYPE_BLOCK))
        {
            return 0;
        }

        if (moduleType.equals(ModuleType.TYPE_ENDERCORE) &&
           (tier < ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC || tier > ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED))
        {
            return 0;
        }

        return this.getMaxModules(containerStack, moduleType);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return DropsMode.fromStack(stack) == DropsMode.REMOTE ? super.getItemStackDisplayName(stack) : this.getBaseItemDisplayName(stack);
    }

    @SideOnly(Side.CLIENT)
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
        int coreTier = this.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE);
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String preDGreen = TextFormatting.DARK_GREEN.toString();
        String preBlue = TextFormatting.BLUE.toString();

        // Drops mode
        DropsMode mode = DropsMode.fromStack(stack);
        boolean powered = PowerStatus.fromStack(stack) == PowerStatus.POWERED;
        String str = (mode == DropsMode.NORMAL ? "enderutilities.tooltip.item.normal"
                    : mode == DropsMode.PLAYER ? "enderutilities.tooltip.item.endertool.playerinv"
                    : "enderutilities.tooltip.item.endertool.remote");
        str = I18n.format(str);
        list.add(I18n.format("enderutilities.tooltip.item.endertool.dropsmode") + ": " + preDGreen + str + rst);

        if (ToolType.fromStack(stack).equals(ToolType.HOE))
        {
            str = (powered ? "enderutilities.tooltip.item.3x3" : "enderutilities.tooltip.item.1x1");
            str = I18n.format(str);
            list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + str + rst);
        }
        else
        {
            // Dig mode (normal/fast)
            str = (powered ? "enderutilities.tooltip.item.fast" : "enderutilities.tooltip.item.normal");
            str = I18n.format(str);
            list.add(I18n.format("enderutilities.tooltip.item.endertool.digmode") + ": " + preDGreen + str + rst);
        }

        // Installed Ender Core type
        str = I18n.format("enderutilities.tooltip.item.endercore") + ": ";
        if (coreTier >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC && coreTier <= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED)
        {
            String coreType = (coreTier == ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC ? "enderutilities.tooltip.item.basic" :
                              (coreTier == ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ENHANCED ? "enderutilities.tooltip.item.enhanced" :
                                      "enderutilities.tooltip.item.advanced"));
            coreType = I18n.format(coreType);
            str += preDGreen + coreType + rst + " (" + preBlue + I18n.format("enderutilities.tooltip.item.tier") +
                    " " + (coreTier + 1) + rst + ")";
        }
        else
        {
            String preRed = TextFormatting.RED.toString();
            str += preRed + I18n.format("enderutilities.tooltip.item.none") + rst;
        }
        list.add(str);

        if (this.getInstalledModuleCount(stack, ModuleType.CREATIVE_BREAKING) > 0)
        {
            str = TextFormatting.GREEN.toString() + I18n.format("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = TextFormatting.RED.toString() + I18n.format("enderutilities.tooltip.item.no") + rst;
        }

        list.add(I18n.format("enderutilities.tooltip.item.creative_breaking_installed", str));

        if (this.isCreativeLikeBreakingEnabled(stack))
        {
            str = TextFormatting.GREEN.toString() + I18n.format("enderutilities.tooltip.item.yes") + rst;
        }
        else
        {
            str = TextFormatting.RED.toString() + I18n.format("enderutilities.tooltip.item.no") + rst;
        }

        list.add(I18n.format("enderutilities.tooltip.item.creative_breaking_enabled", str));

        // Link Crystals installed
        if (linkCrystalStack != null && linkCrystalStack.getItem() instanceof ItemLinkCrystal)
        {
            String preWhiteIta = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
            // Valid target set in the currently selected Link Crystal
            if (TargetData.itemHasTargetTag(linkCrystalStack))
            {
                ((ItemLinkCrystal)linkCrystalStack.getItem()).addInformationSelective(linkCrystalStack, player, list, advancedTooltips, verbose);
            }
            else
            {
                list.add(I18n.format("enderutilities.tooltip.item.notargetset"));
            }

            int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
            int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
            String dName = (linkCrystalStack.hasDisplayName() ? preWhiteIta + linkCrystalStack.getDisplayName() + rst + " " : "");
            list.add(I18n.format("enderutilities.tooltip.item.selectedlinkcrystal.short") +
                    String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.nolinkcrystals"));
        }

        // Capacitor installed
        if (capacitorStack != null && capacitorStack.getItem() instanceof ItemEnderCapacitor)
        {
            ((ItemEnderCapacitor)capacitorStack.getItem()).addInformationSelective(capacitorStack, player, list, advancedTooltips, verbose);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < 4; i++)
        {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return false;
    }

    public enum DropsMode
    {
        NORMAL ("enderutilities.tooltip.item.normal"),
        PLAYER ("enderutilities.tooltip.item.endertool.playerinv"),
        REMOTE ("enderutilities.tooltip.item.endertool.remote");

        //private final int id;
        private final String unlocalized;

        private DropsMode(String unlocalized)
        {
            //this.id = id;
            this.unlocalized = unlocalized;
        }

        public static DropsMode fromStack(ItemStack stack)
        {
            int mode = MathHelper.clamp(NBTUtils.getByte(stack, null, "DropsMode"), 0, 2);
            return values()[mode];
        }

        public String getDisplayName()
        {
            return I18n.format(this.unlocalized);
        }
    }

    public enum PowerStatus
    {
        UNPOWERED,
        POWERED;

        private PowerStatus()
        {
        }

        public static PowerStatus fromStack(ItemStack stack)
        {
            int mode = MathHelper.clamp(NBTUtils.getByte(stack, null, "Powered"), 0, 1);
            return values()[mode];
        }
    }

    public enum CreativeBreaking
    {
        DISABLED,
        ENABLED;

        private CreativeBreaking()
        {
        }

        public static CreativeBreaking fromStack(ItemStack stack)
        {
            int mode = MathHelper.clamp(NBTUtils.getByte(stack, null, "CreativeBreaking"), 0, 1);
            return values()[mode];
        }
    }

    public enum ToolType
    {
        PICKAXE (0, "pickaxe",  ReferenceNames.NAME_ITEM_ENDER_PICKAXE, 5.0f, -2.7f),
        AXE     (1, "axe",      ReferenceNames.NAME_ITEM_ENDER_AXE,     9.0f, -2.9f),
        SHOVEL  (2, "shovel",   ReferenceNames.NAME_ITEM_ENDER_SHOVEL,  5.5f, -2.9f),
        HOE     (3, "hoe",      ReferenceNames.NAME_ITEM_ENDER_HOE,     1.0f,  0.1f),
        INVALID (-1, "null",    "null",                                 0.0f,  0.0f);

        private final int id;
        private final String toolClass;
        private final String name;
        private final float attackDamage;
        private final float attackSpeed;

        private static final Map<String, ToolType> TYPES = new HashMap<String, ToolType>();

        static
        {
            for (ToolType type : ToolType.values())
            {
                TYPES.put(type.getToolClass(), type);
            }
        }

        private ToolType(int id, String toolClass, String name, float attackDamage, float attackSpeed)
        {
            this.id = id;
            this.toolClass = toolClass;
            this.name = name;
            this.attackDamage = attackDamage;
            this.attackSpeed = attackSpeed;
        }

        public int getId()
        {
            return this.id;
        }

        public String getName()
        {
            return this.name;
        }

        public float getAttackDamage()
        {
            return this.attackDamage;
        }

        public float getAttackSpeed()
        {
            return this.attackSpeed;
        }

        public String getToolClass()
        {
            return this.toolClass;
        }

        public boolean equals(ToolType other)
        {
            return this.id == other.id;
        }

        public static ToolType fromToolClass(String toolClass)
        {
            ToolType type = TYPES.get(toolClass);
            return type != null ? type : SHOVEL;
        }

        public static ToolType fromStack(ItemStack stack)
        {
            int meta = MathHelper.clamp(stack.getMetadata(), 0, values().length - 2);
            return values()[meta];
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        return new ResourceLocation[] { new ModelResourceLocation(Reference.MOD_ID + ":item_endertool", "inventory") };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        return new ModelResourceLocation(Reference.MOD_ID + ":item_endertool", "inventory");
    }
}
