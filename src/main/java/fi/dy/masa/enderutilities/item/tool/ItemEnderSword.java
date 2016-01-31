package fi.dy.masa.enderutilities.item.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.client.effects.Particles;
import fi.dy.masa.enderutilities.creativetab.CreativeTab;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ILocationBound;
import fi.dy.masa.enderutilities.item.base.IModular;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.reference.ReferenceKeys;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.EnergyBridgeTracker;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperPlayer;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.UtilItemModular;

public class ItemEnderSword extends ItemSword implements IKeyBound, IModular
{
    public static final int ENDER_CHARGE_COST = 50;
    public static final int MODE_SUMMON = 3;
    private float damageVsEntity;
    private final Item.ToolMaterial material;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;
    @SideOnly(Side.CLIENT)
    private IIcon iconEmpty;
    @SideOnly(Side.CLIENT)
    String[] parts;

    public ItemEnderSword()
    {
        super(ItemEnderTool.ENDER_ALLOY_ADVANCED);
        this.material = ItemEnderTool.ENDER_ALLOY_ADVANCED;
        this.setMaxStackSize(1);
        this.setMaxDamage(this.material.getMaxUses());
        this.setNoRepair();
        this.damageVsEntity = 5.0f + this.material.getDamageVsEntity();
        this.setCreativeTab(CreativeTab.ENDER_UTILITIES_TAB);
        this.setUnlocalizedName(ReferenceNames.getPrefixedName(ReferenceNames.NAME_ITEM_ENDER_SWORD));
        this.setTextureName(ReferenceTextures.getItemTextureName(ReferenceNames.NAME_ITEM_ENDER_SWORD));
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
    @Override
    public float func_150931_i()
    {
        // FIXME no way to check if the item is broken without ItemStack and NBT data
        return this.damageVsEntity;
    }

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

    @Override
    public int getMaxDamage(ItemStack stack)
    {
        return this.material.getMaxUses();
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
    public float func_150893_a(ItemStack stack, Block block)
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
        if (targetEntity != null && targetEntity.worldObj.isRemote == false && this.getSwordMode(stack) == MODE_SUMMON)
        {
            this.summonFighterEndermen(targetEntity.worldObj, targetEntity, 3);
        }

        return this.addToolDamage(stack, 1, targetEntity, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z, EntityLivingBase livingbase)
    {
        if (block.getBlockHardness(world, x, y, z) != 0.0f && this.isToolBroken(stack) == false)
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

    private IInventory getLinkedInventoryWithChecks(ItemStack toolStack, EntityPlayer player)
    {
        byte mode = this.getSwordMode(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == 0)
        {
            return null;
        }

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == 1 && (player instanceof FakePlayer) == false && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 0)
        {
            return player.inventory;
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == 2 && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE_ACTIVE) >= 1
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

        byte mode = this.getSwordMode(toolStack);
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters

        if (mode == 0 || mode == MODE_SUMMON)
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
            if (mode == 2 || MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, item)) == false)
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
                        ItemStack stackTmp = InventoryUtils.tryInsertItemStackToInventory(inv, stack.copy(), target.blockFace);
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
            ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension, target.posX >> 4, target.posZ >> 4, 30);

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
                        Particles.spawnParticles(targetWorld, "portal", target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
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
            if (mode == 2)
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

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(x - r, y - r, z - r, x + r, y + r, z + r);
        List<EntityEndermanFighter> list = world.getEntitiesWithinAABB(EntityEndermanFighter.class, bb);

        for (EntityEndermanFighter fighter : list)
        {
            if (fighter.getEntityToAttack() == null && fighter.hasCustomNameTag() == false)
            {
                fighter.setTargetCommanded(targetEntity);
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
            Block block = world.getBlock((int)x, (int)targetEntity.posY - 1, (int)z);

            if (world.getCollidingBoundingBoxes(fighter, fighter.boundingBox).isEmpty()  == true && world.isAnyLiquid(fighter.boundingBox) == false
                && block.getMaterial().blocksMovement() == true)
            {
                for (int j = 0; j < 16; ++j)
                {
                    float vx = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vy = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vz = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    world.spawnParticle("portal", x, y, z, vx, vy, vz);
                }

                world.playSoundEffect(x, y, z, "mob.endermen.portal", 1.0F, 1.0F);

                world.spawnEntityInWorld(fighter);
                fighter.setTargetCommanded(targetEntity);

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
    public boolean isRepairable()
    {
        return false;
    }

    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D()
    {
        return true;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.block;
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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(x, y, z);
        // When sneak-right-clicking on an IInventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (player != null && player.isSneaking() == true && te != null && (te instanceof IInventory || te.getClass() == TileEntityEnderChest.class)
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            if (world.isRemote == false)
            {
                UtilItemModular.setTarget(stack, player, x, y, z, side, hitX, hitY, hitZ, false, false);
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
    public boolean func_150897_b(Block block)
    {
        return block == Blocks.web;
    }

    @Override
    public boolean getIsRepairable(ItemStack stack1, ItemStack stack2)
    {
        return false;
    }

    @Override
    public Multimap getAttributeModifiers(ItemStack stack)
    {
        double dmg = this.damageVsEntity;

        // Broken sword, or in Summon fighters mode, only deal minimal damage directly
        if (this.isToolBroken(stack) == true || this.getSwordMode(stack) == 3)
        {
            dmg = 0.0d;
        }

        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", dmg, 0));
        return multimap;
    }

    public byte getSwordMode(ItemStack stack)
    {
        return NBTUtils.getByte(stack, null, "Mode");
    }

    public void cycleSwordMode(ItemStack stack)
    {
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters
        NBTUtils.cycleByteValue(stack, null, "Mode", MODE_SUMMON);
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
    public int getInstalledModuleCount(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getInstalledModuleCount(containerStack, moduleType);
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

    @Override
    public int getMaxModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getMaxModuleTier(containerStack, moduleType);
    }

    public int getSelectedModuleTier(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleTier(containerStack, moduleType);
    }

    @Override
    public ItemStack getSelectedModuleStack(ItemStack containerStack, ModuleType moduleType)
    {
        return UtilItemModular.getSelectedModuleStack(containerStack, moduleType);
    }

    public boolean setSelectedModuleStack(ItemStack containerStack, ModuleType moduleType, ItemStack moduleStack)
    {
        return UtilItemModular.setSelectedModuleStack(containerStack, moduleType, moduleStack);
    }

    @Override
    public boolean changeSelectedModule(ItemStack containerStack, ModuleType moduleType, boolean reverse)
    {
        return UtilItemModular.changeSelectedModule(containerStack, moduleType, reverse);
    }

    @Override
    public List<NBTTagCompound> getAllModules(ItemStack containerStack)
    {
        return UtilItemModular.getAllModules(containerStack);
    }

    @Override
    public boolean setAllModules(ItemStack containerStack, List<NBTTagCompound> modules)
    {
        return UtilItemModular.setAllModules(containerStack, modules);
    }

    @Override
    public boolean setModule(ItemStack containerStack, int index, NBTTagCompound nbt)
    {
        return UtilItemModular.setModule(containerStack, index, nbt);
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
        byte mode = this.getSwordMode(stack);
        String str = (mode == 0 ? "enderutilities.tooltip.item.normal" : mode == 1 ? "enderutilities.tooltip.item.endertool.playerinv" : mode == 2 ? "enderutilities.tooltip.item.endertool.remote" : "enderutilities.tooltip.item.endersword.summon");
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
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips)
    {
        ArrayList<String> tmpList = new ArrayList<String>();
        boolean verbose = EnderUtilities.proxy.isShiftKeyDown();

        // "Fresh" items "without" NBT data: display the tips before the usual tooltip data
        // We check for the ench and Items tags so that creative spawned items won't show the tooltip
        // once they have some other NBT data on them
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
    public boolean hasEffect(ItemStack par1ItemStack, int pass)
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderPasses(int metadata)
    {
        return 5;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.parts = new String[] {"rod", "head.1", "head.2", "head.3", "head.4", "head.1.broken", "head.2.broken", "head.3.broken", "head.4.broken",
                "core.1", "core.2", "core.3", "capacitor.1", "capacitor.2", "capacitor.3", "linkcrystal.1", "linkcrystal.2"};
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".rod");
        this.iconEmpty = iconRegister.registerIcon(ReferenceTextures.getItemTextureName("empty"));
        this.iconArray = new IIcon[this.parts.length];
        String prefix = this.getIconString() + ".";

        for (int i = 0; i < this.parts.length; i++)
        {
            this.iconArray[i] = iconRegister.registerIcon(prefix + this.parts[i]);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(ItemStack stack, int renderPass)
    {
        if (stack == null)
        {
            return this.itemIcon;
        }

        int i = 0;
        int tier = 0;

        switch(renderPass)
        {
            case 0: // 0: Rod
                break;
            case 1: // 1: Head
                i += getSwordMode(stack) + 1;

                // Broken tool
                if (this.isToolBroken(stack) == true)
                {
                    i += 4;
                }
                break;
            case 2: // 2: Core
                tier = this.getMaxModuleTier(stack, ModuleType.TYPE_ENDERCORE_ACTIVE);
                if (tier >= 0)
                {
                    i += tier + 9;
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
                    i += tier + 12;
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
                    i += tier + 15;
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
}
