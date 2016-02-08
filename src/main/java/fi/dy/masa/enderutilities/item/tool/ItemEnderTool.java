package fi.dy.masa.enderutilities.item.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.client.effects.Effects;
import fi.dy.masa.enderutilities.event.PlayerItemPickupEvent;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderTool extends ItemLocationBoundModular
{
    public static final Item.ToolMaterial ENDER_ALLOY_ADVANCED =
        EnumHelper.addToolMaterial(ReferenceNames.NAME_MATERIAL_ENDERALLOY_ADVANCED,
            Configs.harvestLevelEnderAlloyAdvanced.getInt(3), 3120, 12.0f, 4.0f, 15);

    public static final int ENDER_CHARGE_COST = 50;
    public float efficiencyOnProperMaterial;
    public float damageVsEntity;
    private final Item.ToolMaterial material;

    public ItemEnderTool()
    {
        super();
        this.material = ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setNoRepair();
        this.efficiencyOnProperMaterial = this.material.getEfficiencyOnProperMaterial();
        this.damageVsEntity = 2.0f + this.material.getDamageVsEntity();
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERTOOL);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        ToolType toolType = ToolType.fromStack(stack);
        if (toolType != ToolType.INVALID)
        {
            return super.getUnlocalizedName() + "." + toolType.getName();
        }

        return super.getUnlocalizedName();
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        // When sneak-right-clicking on an IInventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (playerIn != null && playerIn.isSneaking() == true && te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class)
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            if (worldIn.isRemote == false)
            {
                UtilItemModular.setTarget(stack, playerIn,pos, side, hitX, hitY, hitZ, false, false);
            }

            return true;
        }
        // Hoe
        else if (ToolType.fromStack(stack).equals(ToolType.HOE) == true)
        {
            if (worldIn.isRemote == true)
            {
                return true;
            }

            if (PowerStatus.fromStack(stack) == PowerStatus.POWERED)
            {
                // Didn't till any soil; try to plant stuff from the player inventory or a remote inventory
                if (this.useHoeArea(stack, playerIn, worldIn, pos, side, 1, 1) == false)
                {
                    this.useHoeToPlantArea(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ, 1, 1);
                }
            }
            else
            {
                // Didn't till any soil; try to plant stuff from the player inventory or a remote inventory
                if (this.useHoe(stack, playerIn, worldIn, pos, side) == false)
                {
                    this.useHoeToPlant(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
                }
            }
        }
        // Try to place a block from the slot right to the currently selected tool (or from slot 1 if tool is in slot 9)
        else if (playerIn != null && (playerIn instanceof FakePlayer) == false)
        {
            return this.placeBlock(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent)
    {
        super.onUpdate(stack, world, player, slot, isCurrent);

        if (world.isRemote == false && EnergyBridgeTracker.dimensionHasEnergyBridge(world.provider.getDimensionId()) == true &&
            (world.provider.getDimensionId() == 1 || EnergyBridgeTracker.dimensionHasEnergyBridge(1) == true))
        {
            UtilItemModular.addEnderCharge(stack, ItemEnderCapacitor.CHARGE_RATE_FROM_ENERGY_BRIDGE, true);
        }
    }

    private boolean placeBlock(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        int origSlot = playerIn.inventory.currentItem;
        int slot = (origSlot >= InventoryPlayer.getHotbarSize() - 1 ? 0 : origSlot + 1);
        ItemStack targetStack = playerIn.inventory.getStackInSlot(slot);

        // If the tool is in the first slot of the hotbar and there is no ItemBlock in the second slot, we fall back to the last slot
        if (origSlot == 0 && (targetStack == null || (targetStack.getItem() instanceof ItemBlock) == false))
        {
            slot = InventoryPlayer.getHotbarSize() - 1;
            targetStack = playerIn.inventory.getStackInSlot(slot);
        }

        // If the target stack is an ItemBlock, we try to place that in the world
        if (targetStack != null && targetStack.getItem() instanceof ItemBlock)
        {
            // Check if we can place the block
            if (BlockUtils.checkCanPlaceBlockAt(worldIn, pos, side, playerIn, targetStack) == true)
            {
                if (worldIn.isRemote == true)
                {
                    return true;
                }

                playerIn.inventory.currentItem = slot;
                boolean success = targetStack.onItemUse(playerIn, worldIn, pos, side, hitX, hitY, hitZ);
                playerIn.inventory.currentItem = origSlot;
                playerIn.inventory.markDirty();
                playerIn.inventoryContainer.detectAndSendChanges();
                return success;
            }
        }

        return false;
    }

    public boolean useHoeArea(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, int rWidth, int rHeight)
    {
        boolean northSouth = (((int)MathHelper.floor_float(player.rotationYaw * 4.0f / 360.0f + 0.5f)) & 1) == 0;
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

        if (side != EnumFacing.DOWN && world.isAirBlock(pos.up()) == true)
        {
            IBlockState newBlockState = null;

            if (block == Blocks.grass)
            {
                newBlockState = Blocks.farmland.getDefaultState();
            }
            else if (block == Blocks.dirt)
            {
                if (state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT)
                {
                    newBlockState = Blocks.farmland.getDefaultState();
                }
                else if (state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.COARSE_DIRT)
                {
                    newBlockState = Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT);
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

            SoundType sound = newBlockState.getBlock().stepSound;
            world.playSoundEffect(pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d,
                    sound.getStepSound(), (sound.getVolume() + 1.0f) / 2.0f, sound.getFrequency() * 0.8f);

            if (world.isRemote == false)
            {
                world.setBlockState(pos, newBlockState);
                // 0.4.2: No idea why this is needed to get the blocks to update to the client, as it should be called from setBlock() already...
                world.markBlockForUpdate(pos);
                this.addToolDamage(stack, 1, player, player);
            }

            return true;
        }

        return false;
    }

    public boolean useHoeToPlantArea(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int rWidth, int rHeight)
    {
        boolean northSouth = (((int)MathHelper.floor_float(player.rotationYaw * 4.0f / 360.0f + 0.5f)) & 1) == 0;
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
        if (UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false) == false)
        {
            return false;
        }

        IInventory inv = this.getLinkedInventoryWithChecks(toolStack, player);

        if (inv != null)
        {
            if (inv instanceof ISidedInventory)
            {
                NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);
                ISidedInventory sided = (ISidedInventory) inv;
                int[] slots = sided.getSlotsForFace(target.facing);
                for (int slotNum : slots)
                {
                    if (sided.canExtractItem(slotNum, sided.getStackInSlot(slotNum), target.facing) == true
                        && this.plantItemFromInventorySlot(world, player, sided, slotNum, pos, side, hitX, hitY, hitZ) == true)
                    {
                        UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true);

                        Effects.addItemTeleportEffects(world, pos);
                    }
                }
            }
            else
            {
                int size = inv.getSizeInventory();
                for (int slotNum = 0; slotNum < size; ++slotNum)
                {
                    if (this.plantItemFromInventorySlot(world, player, inv, slotNum, pos, side, hitX, hitY, hitZ) == true)
                    {
                        // Use Ender Charge if planting from a remote inventory
                        if (DropsMode.fromStack(toolStack) == DropsMode.REMOTE)
                        {
                            UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true);
                        }

                        Effects.addItemTeleportEffects(world, pos);
                    }
                }
            }
        }

        return false;
    }

    private boolean plantItemFromInventorySlot(World world, EntityPlayer player, IInventory inv, int slotNum, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        ItemStack plantStack = inv.getStackInSlot(slotNum);
        if (plantStack != null && plantStack.getItem() instanceof IPlantable)
        {
            ItemStack newStack = plantStack.copy();
            if (newStack.onItemUse(player, world, pos, side, hitX, hitY, hitZ) == true)
            {
                if (newStack.stackSize > 0)
                {
                    inv.setInventorySlotContents(slotNum, newStack);
                }
                else
                {
                    inv.setInventorySlotContents(slotNum, null);
                }

                inv.markDirty();

                if (inv instanceof InventoryPlayer)
                {
                    player.inventoryContainer.detectAndSendChanges();
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isItemTool(ItemStack stack)
    {
        return true;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack)
    {
        //System.out.println("getToolClasses()");
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
        return false;
    }

    @Override
    public void setDamage(ItemStack stack, int damage)
    {
        damage = MathHelper.clamp_int(damage, 0, this.material.getMaxUses());
        NBTUtils.setShort(stack, null, "ToolDamage", (short)damage);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        return this.getToolDamage(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        return (double)this.getToolDamage(stack) / (double)this.getMaxDamage(stack);
    }

    public boolean isToolBroken(ItemStack stack)
    {
        return NBTUtils.getShort(stack, null, "ToolDamage") >= this.material.getMaxUses();
    }

    public int getToolDamage(ItemStack stack)
    {
        return NBTUtils.getShort(stack, null, "ToolDamage");
    }

    public boolean addToolDamage(ItemStack stack, int amount, EntityLivingBase living1, EntityLivingBase living2)
    {
        //System.out.println("addToolDamage(): living1: " + living1 + " living2: " + living2 + " remote: " + living2.worldObj.isRemote);
        if (stack == null || this.isToolBroken(stack) == true)
        {
            return false;
        }

        if (amount > 0)
        {
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
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

        int damage = NBTUtils.getShort(stack, null, "ToolDamage");
        damage = Math.min(damage + amount, this.material.getMaxUses());
        this.setDamage(stack, damage);

        // Tool just broke
        if (damage == this.material.getMaxUses())
        {
            living1.renderBrokenItemStack(stack);
        }

        return true;
    }

    /**
     * Repair the tool by the provided amount of uses.
     * If amount is -1, then the tool will be fully repaired.
     */
    public void repairTool(ItemStack stack, int amount)
    {
        if (amount == -1)
        {
            amount = this.material.getMaxUses();
        }

        int damage = Math.max(NBTUtils.getShort(stack, null, "ToolDamage") - amount, 0);

        this.setDamage(stack, damage);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
    {
        this.addToolDamage(stack, 2, living1, living2);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, BlockPos pos, EntityLivingBase living)
    {
        //System.out.println("onBlockDestroyed(): living: " + living + " remote: " + living.worldObj.isRemote);

        // Don't use durability for breaking leaves with an axe
        if (block.getMaterial() != null && block.getMaterial() == Material.leaves && ToolType.fromStack(stack).equals(ToolType.AXE))
        {
            return false;
        }

        // Don't use durability on instant-minable blocks (hardness == 0.0f), or if the tool is already broken
        if (this.isToolBroken(stack) == false && block.getBlockHardness(world, pos) > 0.0f)
        {
            // Fast mode uses double the durability
            int dmg = (PowerStatus.fromStack(stack) == PowerStatus.POWERED ? 2 : 1);

            this.addToolDamage(stack, dmg, living, living);
            return true;
        }

        return false;
    }

    private IInventory getLinkedInventoryWithChecks(ItemStack toolStack, EntityPlayer player)
    {
        DropsMode mode = DropsMode.fromStack(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == DropsMode.NORMAL)
        {
            return null;
        }

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == DropsMode.PLAYER && (player instanceof FakePlayer) == false &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC)
        {
            return player.inventory;
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == DropsMode.REMOTE &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ENHANCED &&
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false) == true)
        {
            return UtilItemModular.getBoundInventory(toolStack, player, 30);
        }

        return null;
    }

    public void handleHarvestDropsEvent(ItemStack toolStack, HarvestDropsEvent event)
    {
        if (this.isToolBroken(toolStack) == true || event.world == null || event.world.isRemote == true)
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

        EntityPlayer player = event.harvester;
        boolean isSilk = event.isSilkTouching;
        int numDropsOriginal = event.drops.size();

        // Don't try to handle the drops via other means in the Remote mode
        if (mode != DropsMode.REMOTE && MinecraftForge.EVENT_BUS.post(new PlayerItemPickupEvent(player, event.drops)) == true)
        {
            Effects.addItemTeleportEffects(event.world, event.pos);
            return;
        }

        IInventory inv = this.getLinkedInventoryWithChecks(toolStack, player);
        if (inv != null)
        {
            Iterator<ItemStack> iter = event.drops.iterator();

            if (inv instanceof InventoryPlayer)
            {
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
            else
            {
                NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

                while (iter.hasNext() == true)
                {
                    ItemStack stack = iter.next();
                    if (stack != null && (isSilk || event.world.rand.nextFloat() < event.dropChance))
                    {
                        ItemStack stackTmp = InventoryUtils.tryInsertItemStackToInventory(inv, stack.copy(), target.facing);
                        if (stackTmp == null)
                        {
                            iter.remove();
                        }
                        else
                        {
                            stack.stackSize = stackTmp.stackSize;
                        }
                    }
                }
            }
        }
        // Location type Link Crystal, teleport/spawn the drops as EntityItems to the target spot
        else if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION)
        {
            NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

            // For cross-dimensional item teleport we require the third tier of active Ender Core
            if (NBTHelperPlayer.canAccessSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL, player) == false
                || (target.dimension != player.dimension &&
                    this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) != ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED))
            {
                return;
            }

            World targetWorld = MinecraftServer.getServer().worldServerForDimension(target.dimension);
            if (targetWorld == null)
            {
                return;
            }

            // Chunk load the target for 30 seconds
            ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4, 30);

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
                        Effects.spawnParticles(targetWorld, EnumParticleTypes.PORTAL, target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
                        iter.remove();
                    }
                }
            }
        }

        // At least something got transported somewhere...
        if (event.drops.size() != numDropsOriginal)
        {
            // Transported the drops to somewhere remote
            if (mode == DropsMode.REMOTE)
            {
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true);
            }

            Effects.addItemTeleportEffects(event.world, event.pos);
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
    public boolean canHarvestBlock(Block block, ItemStack stack)
    {
        if (this.isToolBroken(stack) == true)
        {
            return false;
        }

        ToolType tool = ToolType.fromStack(stack);
        if (tool.equals(ToolType.PICKAXE)) // Ender Pickaxe
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
        else if (tool.equals(ToolType.AXE)) // Ender Axe
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
        else if (tool.equals(ToolType.SHOVEL)) // Ender Shovel
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

    @Override
    public float getDigSpeed(ItemStack stack, IBlockState iBlockState)
    {
        if (this.isToolBroken(stack) == true)
        {
            return 0.2f;
        }

        ToolType tool = ToolType.fromStack(stack);
        Block block = iBlockState.getBlock();
        // Allow instant mine of leaves with the axe
        if (block.getMaterial() != null && block.getMaterial() == Material.leaves && tool.equals(ToolType.AXE))
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

        //if (ForgeHooks.isToolEffective(stack, block, meta))
        if (block.isToolEffective(tool.getToolClass(), iBlockState) == true)
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
        return super.getDigSpeed(stack, iBlockState);
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass)
    {
        //System.out.println("getHarvestLevel(stack, \"" + toolClass + "\")");
        if (stack != null && this.isToolBroken(stack) == false && toolClass.equals(ToolType.fromStack(stack).getToolClass()) == true)
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
    public Multimap<String, AttributeModifier> getAttributeModifiers(ItemStack stack)
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
            dmg += ToolType.fromStack(stack).getAttackDamage();
        }

        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Tool modifier", dmg, 0));
        return multimap;
    }

    public void cyclePoweredMode(ItemStack stack)
    {
        NBTUtils.cycleByteValue(stack, null, "Powered", 1);
    }

    public void cycleDropsMode(ItemStack stack)
    {
        NBTUtils.cycleByteValue(stack, null, "DropsMode", 2);
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
            this.cyclePoweredMode(stack);
        }
        // Ctrl + (Shift + ) Toggle mode
        else if (ReferenceKeys.keypressContainsControl(key) == true && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.changeSelectedModule(stack, ModuleType.TYPE_LINKCRYSTAL, ReferenceKeys.keypressActionIsReversed(key) || ReferenceKeys.keypressContainsShift(key));
        }
        // Shift + Alt + Toggle mode: Store the player's current location
        else if (ReferenceKeys.keypressContainsShift(key) == true
                && ReferenceKeys.keypressContainsAlt(key) == true
                && ReferenceKeys.keypressContainsControl(key) == false)
        {
            UtilItemModular.setTarget(stack, player, true);
        }
        // Shift + Toggle mode: Toggle the block drops handling mode: normal, player, remote
        else if (ReferenceKeys.keypressContainsShift(key) == true
                && ReferenceKeys.keypressContainsControl(key) == false
                && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.cycleDropsMode(stack);
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
    public int getMaxModules(ItemStack containerStack)
    {
        return 5;
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
        if (moduleType.equals(ModuleType.TYPE_LINKCRYSTAL) == true &&
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

    @SideOnly(Side.CLIENT)
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
        int coreTier = this.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE);
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String preBlue = EnumChatFormatting.BLUE.toString();

        // Drops mode
        DropsMode mode = DropsMode.fromStack(stack);
        boolean powered = PowerStatus.fromStack(stack) == PowerStatus.POWERED;
        String str = (mode == DropsMode.NORMAL ? "enderutilities.tooltip.item.normal"
                    : mode == DropsMode.PLAYER ? "enderutilities.tooltip.item.endertool.playerinv"
                    : "enderutilities.tooltip.item.endertool.remote");
        str = StatCollector.translateToLocal(str);
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.endertool.dropsmode") + ": " + preDGreen + str + rst);

        if (ToolType.fromStack(stack).equals(ToolType.HOE) == true)
        {
            str = (powered == true ? "enderutilities.tooltip.item.3x3" : "enderutilities.tooltip.item.1x1");
            str = StatCollector.translateToLocal(str);
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + preDGreen + str + rst);
        }
        else
        {
            // Dig mode (normal/fast)
            str = (powered == true ? "enderutilities.tooltip.item.fast" : "enderutilities.tooltip.item.normal");
            str = StatCollector.translateToLocal(str);
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.endertool.digmode") + ": " + preDGreen + str + rst);
        }

        // Installed Ender Core type
        str = StatCollector.translateToLocal("enderutilities.tooltip.item.endercore") + ": ";
        if (coreTier >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC && coreTier <= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED)
        {
            String coreType = (coreTier == ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC ? "enderutilities.tooltip.item.basic" :
                              (coreTier == ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ENHANCED ? "enderutilities.tooltip.item.enhanced" :
                                      "enderutilities.tooltip.item.advanced"));
            coreType = StatCollector.translateToLocal(coreType);
            str += preDGreen + coreType + rst + " (" + preBlue + StatCollector.translateToLocal("enderutilities.tooltip.item.tier") +
                    " " + (coreTier + 1) + rst + ")";
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

            int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
            int sel = UtilItemModular.getClampedModuleSelection(stack, ModuleType.TYPE_LINKCRYSTAL) + 1;
            String dName = (linkCrystalStack.hasDisplayName() ? preWhiteIta + linkCrystalStack.getDisplayName() + rst + " " : "");
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.selectedlinkcrystal.short") +
                    String.format(" %s(%s%d%s / %s%d%s)", dName, preBlue, sel, rst, preBlue, num, rst));
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

        if (advancedTooltips == true)
        {
            str = StatCollector.translateToLocal("enderutilities.tooltip.item.durability");
            list.add(str + ": " + (this.getMaxDamage(stack) - this.getToolDamage(stack)) + " / " + this.getMaxDamage(stack));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        if (Configs.disableItemEnderTools.getBoolean(false) == false)
        {
            for (int i = 0; i < 4; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }
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
            int mode = MathHelper.clamp_int(NBTUtils.getByte(stack, null, "DropsMode"), 0, 2);
            return values()[mode];
        }

        public String getDisplayName()
        {
            return StatCollector.translateToLocal(this.unlocalized);
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
            int mode = MathHelper.clamp_int(NBTUtils.getByte(stack, null, "Powered"), 0, 1);
            return values()[mode];
        }
    }

    public enum ToolType
    {
        PICKAXE (0, "pickaxe",  ReferenceNames.NAME_ITEM_ENDER_PICKAXE, 2.0f),
        AXE     (1, "axe",      ReferenceNames.NAME_ITEM_ENDER_AXE,     3.0f),
        SHOVEL  (2, "shovel",   ReferenceNames.NAME_ITEM_ENDER_SHOVEL,  1.0f),
        HOE     (3, "hoe",      ReferenceNames.NAME_ITEM_ENDER_HOE,     0.0f),
        INVALID (-1, "null",    "null",                                 0.0f);

        private final int id;
        private final String toolClass;
        private final String name;
        private final float attackDamage;

        private static final Map<String, ToolType> mapType = new HashMap<String, ToolType>();

        static
        {
            for (ToolType type : ToolType.values())
            {
                mapType.put(type.getToolClass(), type);
            }
        }

        private ToolType(int id, String toolClass, String name, float attackDamage)
        {
            this.id = id;
            this.toolClass = toolClass;
            this.name = name;
            this.attackDamage = attackDamage;
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
            ToolType type = mapType.get(toolClass);
            return type != null ? type : SHOVEL;
        }

        public static ToolType fromStack(ItemStack stack)
        {
            int meta = MathHelper.clamp_int(stack.getItemDamage(), 0, 3);
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
