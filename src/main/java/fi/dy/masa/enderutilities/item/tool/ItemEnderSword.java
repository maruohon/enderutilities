package fi.dy.masa.enderutilities.item.tool;

import java.util.Iterator;
import java.util.List;
import com.google.common.collect.Multimap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import fi.dy.masa.enderutilities.effects.Effects;
import fi.dy.masa.enderutilities.entity.EntityEndermanFighter;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.item.part.ItemEnderCapacitor;
import fi.dy.masa.enderutilities.item.part.ItemEnderPart;
import fi.dy.masa.enderutilities.item.part.ItemLinkCrystal;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.HotKeys.EnumKey;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.ChunkLoading;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;
import fi.dy.masa.enderutilities.util.nbt.OwnerData;
import fi.dy.masa.enderutilities.util.nbt.TargetData;
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
        this.damageVsEntity = 7.0f;
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_SWORD);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        return SwordMode.fromStack(stack) == SwordMode.REMOTE ? super.getItemStackDisplayName(stack) : this.getBaseItemDisplayName(stack);
    }

    public boolean addToolDamage(ItemStack stack, int amount, EntityLivingBase living1, EntityLivingBase living2)
    {
        if (stack == null || this.isToolBroken(stack))
        {
            return false;
        }

        amount = Math.min(amount, this.getMaxDamage(stack) - stack.getItemDamage());
        stack.damageItem(amount, living2);

        // Tool just broke
        if (this.isToolBroken(stack))
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
    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        if (this.isToolBroken(stack))
        {
            return 0.2f;
        }

        if (state.getBlock() == Blocks.WEB)
        {
            return 15.0f;
        }

        Material material = state.getMaterial();
        if (material == Material.PLANTS ||
            material == Material.VINE ||
            material == Material.CORAL ||
            material == Material.LEAVES ||
            material == Material.GOURD)
        {
            return 1.5f;
        }

        return 1.0f;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase targetEntity, EntityLivingBase attacker)
    {
        // Summon fighters mode
        if (targetEntity != null && targetEntity.getEntityWorld().isRemote == false && SwordMode.fromStack(stack) == SwordMode.SUMMON)
        {
            this.summonFighterEndermen(targetEntity.getEntityWorld(), targetEntity, 3);
        }

        return this.addToolDamage(stack, 1, targetEntity, attacker);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase livingBase)
    {
        if (state.getBlockHardness(world, pos) != 0.0f && this.isToolBroken(stack) == false)
        {
            int amount = Math.min(2, this.getMaxDamage(stack) - stack.getItemDamage());
            stack.damageItem(amount, livingBase);

            // Tool just broke
            if (this.isToolBroken(stack))
            {
                livingBase.renderBrokenItemStack(stack);
            }

            return true;
        }

        return false;
    }

    private IItemHandler getLinkedInventoryWithChecks(ItemStack toolStack, EntityPlayer player)
    {
        SwordMode mode = SwordMode.fromStack(toolStack);
        // Modes: 0: normal; 1: Add drops to player's inventory; 2: Transport drops to Link Crystal's bound destination

        // 0: normal mode; do nothing
        if (mode == SwordMode.NORMAL)
        {
            return null;
        }

        // 1: Add drops to player's inventory; To allow this, we require at least the lowest tier Ender Core (active) installed
        if (mode == SwordMode.PLAYER && this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC)
        {
            return player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP); // main inventory
        }

        // 2: Teleport drops to the Link Crystal's bound target; To allow this, we require an active second tier Ender Core
        else if (mode == SwordMode.REMOTE &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ENHANCED &&
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, true))
        {
            return UtilItemModular.getBoundInventory(toolStack, player, 15);
        }

        return null;
    }

    private ItemStack tryTeleportItems(ItemStack itemsIn, ItemStack toolStack, EntityPlayer player)
    {
        TargetData target = TargetData.getTargetFromSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL);

        // For cross-dimensional item teleport we require the third tier of active Ender Core
        if (OwnerData.canAccessSelectedModule(toolStack, ModuleType.TYPE_LINKCRYSTAL, player) == false
            || (target.dimension != player.dimension &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) != ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_ADVANCED))
        {
            return itemsIn;
        }

        World targetWorld = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(target.dimension);
        if (targetWorld == null)
        {
            return itemsIn;
        }

        // Chunk load the target for 30 seconds
        ChunkLoading.getInstance().loadChunkForcedWithPlayerTicket(player, target.dimension, target.pos.getX() >> 4, target.pos.getZ() >> 4, 30);

        EntityItem entityItem = new EntityItem(targetWorld, target.dPosX, target.dPosY + 0.125d, target.dPosZ, itemsIn.copy());
        entityItem.motionX = entityItem.motionZ = 0.0d;
        entityItem.motionY = 0.15d;

        if (targetWorld.spawnEntity(entityItem))
        {
            Effects.spawnParticles(targetWorld, EnumParticleTypes.PORTAL, target.dPosX, target.dPosY, target.dPosZ, 3, 0.2d, 1.0d);
            return null;
        }

        return itemsIn;
    }

    public void handleLivingDropsEvent(ItemStack toolStack, LivingDropsEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote || this.isToolBroken(toolStack) || event.getDrops() == null || event.getDrops().size() == 0)
        {
            return;
        }

        List<EntityItem> drops = event.getDrops();

        SwordMode mode = SwordMode.fromStack(toolStack);
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters

        if (mode == SwordMode.NORMAL || mode == SwordMode.SUMMON)
        {
            return;
        }

        boolean transported = false;
        EntityPlayer player = (EntityPlayer)event.getSource().getSourceOfDamage();
        Iterator<EntityItem> iter = drops.iterator();
        IItemHandler inv = this.getLinkedInventoryWithChecks(toolStack, player);

        while (iter.hasNext())
        {
            EntityItem item = iter.next();
            ItemStack stack = item.getEntityItem();
            if (stack == null || stack.getItem() == null)
            {
                iter.remove();
                continue;
            }

            ItemStack stackTmp = stack;

            // Don't try to handle the drops via other means in the Remote mode until after we try to transport them here first
            if (mode == SwordMode.PLAYER &&
                this.getMaxModuleTier(toolStack, ModuleType.TYPE_ENDERCORE) >= ItemEnderPart.ENDER_CORE_TYPE_ACTIVE_BASIC &&
                MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, item)))
            {
                Effects.addItemTeleportEffects(player.getEntityWorld(), player.getPosition());
                stackTmp = null;
            }
            else if (inv != null)
            {
                stackTmp = InventoryUtils.tryInsertItemStackToInventory(inv, stack.copy());
            }
            // Location type Link Crystal, teleport/spawn the drops as EntityItems to the target spot
            else if (this.getSelectedModuleTier(toolStack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_LOCATION)
            {
                stackTmp = this.tryTeleportItems(stack, toolStack, player);
            }

            if (stackTmp == null || stackTmp.stackSize <= 0)
            {
                iter.remove();
                transported = true;
            }
            else if (stackTmp.stackSize != stack.stackSize)
            {
                stack.stackSize = stackTmp.stackSize;
                item.setEntityItemStack(stack);
                transported = true;
            }
        }

        // At least something got transported somewhere...
        if (transported)
        {
            // Transported the drops to somewhere remote
            if (mode == SwordMode.REMOTE)
            {
                UtilItemModular.useEnderCharge(toolStack, ENDER_CHARGE_COST, false);
            }

            Entity entity = event.getEntity();
            PacketHandler.INSTANCE.sendToAllAround(
                new MessageAddEffects(MessageAddEffects.EFFECT_ENDER_TOOLS, MessageAddEffects.PARTICLES | MessageAddEffects.SOUND,
                    entity.posX + 0.5d, entity.posY + 0.5d, entity.posZ + 0.5d, 8, 0.2f, 0.3f),
                        new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 24.0d));
        }

        // If we failed to handle the drops ourselves, then try to handle them via other means
        if (drops.size() > 0)
        {
            iter = drops.iterator();
            while (iter.hasNext())
            {
                EntityItem item = iter.next();
                MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, item));

                if (item.isDead || item.getEntityItem() == null || item.getEntityItem().stackSize <= 0)
                {
                    iter.remove();
                }
            }
        }

        if (drops.isEmpty())
        {
            event.setCanceled(true);
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

        AxisAlignedBB bb = new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r);
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
            IBlockState state = world.getBlockState(new BlockPos((int)x, (int)targetEntity.posY - 1, (int)z));

            if (world.getCollisionBoxes(fighter, fighter.getEntityBoundingBox()).isEmpty()  &&
                world.containsAnyLiquid(fighter.getEntityBoundingBox()) == false &&
                state.getMaterial().blocksMovement())
            {
                for (int j = 0; j < 16; ++j)
                {
                    float vx = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vy = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    float vz = (world.rand.nextFloat() - 0.5F) * 0.2F;
                    world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, vx, vy, vz);
                }

                world.playSound(null, x, y, z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, fighter.getSoundCategory(), 1.0F, 1.0F);

                world.spawnEntity(fighter);
                fighter.setPrimaryTarget(targetEntity);

                if (++count >= amount)
                {
                    break;
                }
            }
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return true;
    }

    @Override
    public int getItemEnchantability(ItemStack stack)
    {
        return this.material.getEnchantability();
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        TileEntity te = world.getTileEntity(pos);
        // When sneak-right-clicking on an inventory or an Ender Chest, and the installed Link Crystal is a block type crystal,
        // then bind the crystal to the block clicked on.
        if (player != null && player.isSneaking() && te != null &&
            (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side) || te.getClass() == TileEntityEnderChest.class)
            && UtilItemModular.getSelectedModuleTier(stack, ModuleType.TYPE_LINKCRYSTAL) == ItemLinkCrystal.TYPE_BLOCK)
        {
            if (world.isRemote == false)
            {
                UtilItemModular.setTarget(stack, player, pos, side, hitX, hitY, hitZ, false, false);
            }
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack)
    {
        return state.getBlock() == Blocks.WEB;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack)
    {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot, stack);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            double dmg = this.damageVsEntity;

            // Broken sword, or in Summon fighters mode, only deal minimal damage directly
            if (this.isToolBroken(stack) || SwordMode.fromStack(stack) == SwordMode.SUMMON)
            {
                dmg = 0.0d;
            }

            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", dmg, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.3D, 0));
        }

        return multimap;
    }

    public void cycleSwordMode(ItemStack stack)
    {
        // 3 modes: 0 = normal; 1 = drops to player's inventory; 2 = drops to Link Crystals target; 3 = summon Ender Fighters
        NBTUtils.cycleByteValue(stack, null, "SwordMode", 3);
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Shift + Toggle mode: Toggle the sword mode: normal, drops to player, drops tp remote, summon fighters
        if (EnumKey.TOGGLE.matches(key, HotKeys.MOD_SHIFT))
        {
            this.cycleSwordMode(stack);
        }
        else
        {
            super.doKeyBindingAction(player, stack, key);
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
        SwordMode mode = SwordMode.fromStack(stack);
        String str = (mode == SwordMode.NORMAL ? "enderutilities.tooltip.item.normal"
                    : mode == SwordMode.PLAYER ? "enderutilities.tooltip.item.endertool.playerinv"
                    : mode == SwordMode.REMOTE ? "enderutilities.tooltip.item.endertool.remote"
                    : "enderutilities.tooltip.item.endersword.summon");
        str = I18n.format(str);
        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preDGreen + str + rst);

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
            int mode = MathHelper.clamp(NBTUtils.getByte(stack, null, "SwordMode"), 0, 3);
            return values()[mode];
        }

        public String getDisplayName()
        {
            return I18n.format(this.unlocalized);
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
