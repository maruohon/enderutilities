package fi.dy.masa.enderutilities.item.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.item.base.ILocationBound;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderSword extends ItemLocationBoundModular
{
    public static final int ENDER_CHARGE_COST = 50;
    private float damageVsEntity;
    private final Item.ToolMaterial material;

    public ItemEnderSword()
    {
        super();
        this.material = ItemEnderTool.ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setMaxDamage(this.material.getMaxUses());
        this.setNoRepair();
        this.damageVsEntity = 5.0f + this.material.getDamageVsEntity();
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_SWORD);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        ItemStack moduleStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        if (moduleStack != null && moduleStack.getItem() instanceof ILocationBound)
        {
            String itemName = StatCollector.translateToLocal(this.getUnlocalizedName(stack) + ".name").trim();
            String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE.toString();

            // If the currently selected module has been renamed, show that name
            if (moduleStack.hasDisplayName() == true)
            {
                String pre = EnumChatFormatting.GREEN.toString() + EnumChatFormatting.ITALIC.toString();
                if (itemName.length() >= 14)
                {
                    return EUStringUtils.getInitialsWithDots(itemName) + " " + pre + moduleStack.getDisplayName() + rst;
                }

                return itemName + " " + pre + moduleStack.getDisplayName() + rst;
            }

            // Link Crystal not named
            if (moduleStack.getItem() instanceof ItemLinkCrystal)
            {
                String targetName = ((ItemLinkCrystal)moduleStack.getItem()).getTargetDisplayName(moduleStack);
                if (targetName != null)
                {
                    return itemName + " " + EnumChatFormatting.GREEN.toString() + targetName + rst;
                }
            }
        }

        return super.getItemStackDisplayName(stack);
    }

    // This is used for determining which weapon is better when mobs pick up items
    /*@Override
    public float getDamageVsEntity()
    {
        // FIXME no way to check if the item is broken without ItemStack and NBT data
        return this.damageVsEntity;
    }*/

    public boolean addToolDamage(ItemStack stack, int amount, EntityLivingBase living1, EntityLivingBase living2)
    {
        if (stack == null || this.isToolBroken(stack) == true)
        {
            return false;
        }

        amount = Math.min(amount, this.getMaxDamage(stack) - stack.getItemDamage());
        stack.damageItem(amount, living2);

        // Tool just broke
        if (this.isToolBroken(stack) == true)
        {
            living1.renderBrokenItemStack(stack);
        }

        return true;
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

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase targetEntity, EntityLivingBase attacker)
    {
        // Summon fighters mode
        if (targetEntity != null && targetEntity.worldObj.isRemote == false && SwordMode.fromStack(stack) == SwordMode.SUMMON)
        {
            this.summonFighterEndermen(targetEntity.worldObj, targetEntity, 3);
        }

        return this.addToolDamage(stack, 1, targetEntity, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, BlockPos pos, EntityLivingBase playerIn)
    {
        if (block.getBlockHardness(world, pos) != 0.0f && this.isToolBroken(stack) == false)
        {
            int amount = Math.min(2, this.getMaxDamage(stack) - stack.getItemDamage());
            stack.damageItem(amount, playerIn);

            // Tool just broke
            if (this.isToolBroken(stack) == true)
            {
                playerIn.renderBrokenItemStack(stack);
            }

            return true;
        }

        return false;
    }

    private IInventory getLinkedInventoryWithChecks(ItemStack toolStack, EntityPlayer player)
    {
        SwordMode mode = SwordMode.fromStack(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == SwordMode.NORMAL)
        {
            return null;
        }

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == SwordMode.PLAYER && (player instanceof FakePlayer) == false && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 0)
        {
            return player.inventory;
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == SwordMode.REMOTE && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 1
                && UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false) == true)
        {
            return UtilItemModular.getBoundInventory(toolStack, player, 30);
        }

        return null;
    }

    public void handleLivingDropsEvent(ItemStack toolStack, LivingDropsEvent event)
    {
        if (event.entity.worldObj.isRemote == true || this.isToolBroken(toolStack) == true || event.drops == null || event.drops.size() == 0)
        {
            return;
        }

        SwordMode mode = SwordMode.fromStack(toolStack);
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters

        if (mode == SwordMode.NORMAL || mode == SwordMode.SUMMON)
        {
            return;
        }

        boolean transported = false;
        EntityPlayer player = (EntityPlayer)event.source.getSourceOfDamage();

        // Items to further process by this method
        ArrayList<EntityItem> items = new ArrayList<EntityItem>();

        Iterator<EntityItem> iter = event.drops.iterator();
        while (iter.hasNext() == true)
        {
            EntityItem item = iter.next();

            // Pickup event not canceled, do further processing to those items
            if (mode == SwordMode.REMOTE || MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, item)) == false)
            {
                if (item.getEntityItem() != null && item.getEntityItem().stackSize > 0)
                {
                    items.add(item);
                }

                iter.remove();
            }
            else if (item.getEntityItem() == null || item.getEntityItem().stackSize <= 0)
            {
                iter.remove();
                transported = true;
            }
        }

        IInventory inv = this.getLinkedInventoryWithChecks(toolStack, player);
        if (inv != null)
        {
            iter = items.iterator();

            if (inv instanceof InventoryPlayer)
            {
                while (iter.hasNext() == true)
                {
                    ItemStack stack = iter.next().getEntityItem();
                    if (stack != null)
                    {
                        if (player.inventory.addItemStackToInventory(stack.copy()) == true)
                        {
                            iter.remove();
                            transported = true;
                        }
                    }
                }
            }
            else
            {
                NBTHelperTarget target = NBTHelperTarget.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

                while (iter.hasNext() == true)
                {
                    ItemStack stack = iter.next().getEntityItem();
                    if (stack != null)
                    {
                        ItemStack stackTmp = InventoryUtils.tryInsertItemStackToInventory(inv, stack.copy(), target.facing);
                        if (stackTmp == null)
                        {
                            iter.remove();
                            transported = true;
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
                || (target.dimension != player.dimension && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) < 2))
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

            iter = items.iterator();
            while (iter.hasNext() == true)
            {
                ItemStack stack = iter.next().getEntityItem();
                if (stack != null)
                {
                    EntityItem entityItem = new EntityItem(targetWorld, target.dPosX, target.dPosY + 0.125d, target.dPosZ, stack.copy());
                    entityItem.motionX = entityItem.motionZ = 0.0d;
                    entityItem.motionY = 0.15d;

                    if (targetWorld.spawnEntityInWorld(entityItem) == true)
                    {
                        Particles.spawnParticles(targetWorld, EnumParticleTypes.PORTAL, target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
                        iter.remove();
                        transported = true;
                    }
                }
            }
        }

        // The items that were not handled, are added back to the original event's drops list
        for (EntityItem item : items)
        {
            event.drops.add(item);
        }

        if (event.drops.isEmpty() == true)
        {
            event.setCanceled(true);
        }

        // At least something got transported somewhere...
        if (transported == true)
        {
            // Transported the drops to somewhere remote
            if (mode == SwordMode.REMOTE)
            {
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true);
            }

            PacketHandler.INSTANCE.sendToAllAround(
                new MessageAddEffects(MessageAddEffects.EFFECT_ENDER_TOOLS, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND,
                    event.entity.posX + 0.5d, event.entity.posY + 0.5d, event.entity.posZ + 0.5d, 8, 0.2d, 0.3d),
                        new NetworkRegistry.TargetPoint(event.entity.dimension, event.entity.posX, event.entity.posY, event.entity.posZ, 24.0d));
        }
    }

    private void summonFighterEndermen(World world, EntityLivingBase targetEntity, int amount)
    {
        if (targetEntity instanceof EntityEndermanFighter)
        {
            return;
        }

        double r = 16.0d;
        double x = targetEntity.posX;
        double y = targetEntity.posY;
        double z = targetEntity.posZ;
        int numReTargeted = 0;

        AxisAlignedBB bb = AxisAlignedBB.fromBounds(x - r, y - r, z - r, x + r, y + r, z + r);
        List<EntityEndermanFighter> list = world.getEntitiesWithinAABB(EntityEndermanFighter.class, bb);

        for (EntityEndermanFighter fighter : list)
        {
            if (fighter.getAttackTarget() == null && fighter.hasCustomName() == false)
            {
                fighter.setPrimaryTarget(targetEntity);
                numReTargeted++;
            }
        }

        if (numReTargeted >= amount)
        {
            return;
        }

        int count = numReTargeted;
        for (int i = 0; i < 64; ++i)
        {
            x = targetEntity.posX - 5.0d + world.rand.nextFloat() * 10.0d;
            y = targetEntity.posY - 2.0d + world.rand.nextFloat() * 4.0d;
            z = targetEntity.posZ - 5.0d + world.rand.nextFloat() * 10.0d;

            EntityEndermanFighter fighter = new EntityEndermanFighter(world);
            fighter.setPosition(x, targetEntity.posY, z);
            Block block = world.getBlockState(new BlockPos((int)x, (int)targetEntity.posY - 1, (int)z)).getBlock();

            if (world.getCollidingBoundingBoxes(fighter, fighter.getEntityBoundingBox()).isEmpty()  == true &&
                world.isAnyLiquid(fighter.getEntityBoundingBox()) == false &&
                block.getMaterial().blocksMovement() == true)
            {
                for (int j = 0; j < 16; ++j)
                {
                    float vx = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vy = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vz = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, vy, vz);
                }

                world.playSoundEffect(x, y, z, "mob.endermen.portal", 1.0F, 1.0F);

                world.spawnEntityInWorld(fighter);
                fighter.setPrimaryTarget(targetEntity);

                if (++count >= amount)
                {
                    break;
                }
            }
        }
    }

    @Override
    public boolean isItemTool(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return 72000;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        // When sneak-right-clicking on an IInventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (player != null && player.isSneaking() == true && te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class)
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            if (world.isRemote == false)
            {
                UtilItemModular.setTarget(stack, player, pos, side, hitX, hitY, hitZ, false, false);
            }
            return true;
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

    @Override
    public boolean canHarvestBlock(Block block)
    {
        return block == Blocks.web;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(ItemStack stack)
    {
        double dmg = this.damageVsEntity;

        // Broken sword, or in Summon fighters mode, only deal minimal damage directly
        if (this.isToolBroken(stack) == true || SwordMode.fromStack(stack) == SwordMode.SUMMON)
        {
            dmg = 0.0d;
        }

        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(itemModifierUUID, "Weapon modifier", dmg, 0));
        return multimap;
    }

    public void cycleSwordMode(ItemStack stack)
    {
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters
        NBTUtils.cycleByteValue(stack, null, "SwordMode", 3);
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

        // Ctrl + (Shift + ) Toggle mode
        if (ReferenceKeys.keypressContainsControl(key) == true && ReferenceKeys.keypressContainsAlt(key) == false)
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
        // Shift + Toggle mode: Toggle the sword mode: normal, drops to player, drops tp remote, summon fighters
        else if (ReferenceKeys.keypressContainsShift(key) == true
                && ReferenceKeys.keypressContainsControl(key) == false
                && ReferenceKeys.keypressContainsAlt(key) == false)
        {
            this.cycleSwordMode(stack);
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
    public int getMaxModules(ItemStack containerStack, ItemStack moduleStack)
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
            return this.getMaxModules(containerStack, moduleType);
        }

        return 0;
    }

    @SideOnly(Side.CLIENT)
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        ItemStack linkCrystalStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_LINKCRYSTAL);
        ItemStack capacitorStack = this.getSelectedModuleStack(stack, ModuleType.TYPE_ENDERCAPACITOR);
        int coreTier = this.getSelectedModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
        String rst = EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY.toString();
        String preDGreen = EnumChatFormatting.DARK_GREEN.toString();
        String preBlue = EnumChatFormatting.BLUE.toString();

        // Drops mode
        SwordMode mode = SwordMode.fromStack(stack);
        String str = (mode == SwordMode.NORMAL ? "enderutilities.tooltip.item.normal"
                    : mode == SwordMode.PLAYER ? "enderutilities.tooltip.item.endertool.playerinv"
                    : mode == SwordMode.REMOTE ? "enderutilities.tooltip.item.endertool.remote"
                    : "enderutilities.tooltip.item.endersword.summon");
        str = StatCollector.translateToLocal(str);
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.mode") + ": " + preDGreen + str + rst);

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

            int num = UtilItemModular.getInstalledModuleCount(stack, ModuleType.TYPE_LINKCRYSTAL);
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
    public boolean hasEffect(ItemStack stack)
    {
        return false;
    }

    public static enum SwordMode
    {
        NORMAL ("enderutilities.tooltip.item.normal"),
        PLAYER ("enderutilities.tooltip.item.endertool.playerinv"),
        REMOTE ("enderutilities.tooltip.item.endertool.remote"),
        SUMMON ("enderutilities.tooltip.item.endersword.summon");

        private final String unlocalized;

        private SwordMode(String unlocalized)
        {
            this.unlocalized = unlocalized;
        }

        public static SwordMode fromStack(ItemStack stack)
        {
            int mode = MathHelper.clamp_int(NBTUtils.getByte(stack, null, "SwordMode"), 0, 3);
            return values()[mode];
        }

        public String getDisplayName()
        {
            return StatCollector.translateToLocal(this.unlocalized);
        }
    }

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
