package fi.dy.masa.enderutilities.item.tool;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.Multimap;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IAnvilRepairable;
import fi.dy.masa.enderutilities.item.base.IKeyBound;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.HotKeys;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.BlockUtils;
import fi.dy.masa.enderutilities.util.InventoryUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemVoidPickaxe extends ItemEnderUtilities implements IKeyBound, IAnvilRepairable
{
    public float efficiencyOnProperMaterial;
    private final Item.ToolMaterial material;

    public ItemVoidPickaxe(String name)
    {
        super(name);

        this.material = ItemEnderTool.ENDER_ALLOY_ADVANCED;
        this.efficiencyOnProperMaterial = 6f;

        this.setMaxStackSize(1);
        this.setMaxDamage(this.material.getMaxUses());
        this.setNoRepair();

        this.setHarvestLevel("pickaxe", this.material.getHarvestLevel());
        this.setHarvestLevel("axe", this.material.getHarvestLevel());
        this.setHarvestLevel("shovel", this.material.getHarvestLevel());
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
    public boolean isDamageable()
    {
        return true;
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
        damage = MathHelper.clamp(damage, 0, this.getMaxDamage(stack));
        NBTUtils.setShort(stack, null, "ToolDamage", (short) damage);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack)
    {
        return this.isDamaged(stack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack)
    {
        return (double) this.getDamage(stack) / (double) this.getMaxDamage(stack);
    }

    public boolean isToolBroken(ItemStack stack)
    {
        return this.getDamage(stack) >= this.getMaxDamage(stack);
    }

    private boolean addToolDamage(ItemStack stack, int amount, EntityLivingBase living1, EntityLivingBase living2)
    {
        //System.out.println("addToolDamage(): living1: " + living1 + " living2: " + living2 + " remote: " + living2.worldObj.isRemote);
        if (this.isToolBroken(stack))
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

        if (living1.getEntityWorld().isRemote == false)
        {
            this.setDamage(stack, damage);
        }

        // Tool just broke
        if (damage == this.material.getMaxUses())
        {
            //System.out.printf("tool broke @ %s\n", living1.getEntityWorld().isRemote ? "client" : "server");
            living1.renderBrokenItemStack(stack);
        }

        return true;
    }

    private boolean addToolDamage(ItemStack stack, World world, BlockPos pos, IBlockState state, EntityLivingBase livingBase)
    {
        // Don't use durability on instant-minable blocks (hardness == 0.0f), or if the tool is already broken
        if (this.isToolBroken(stack) == false && state.getBlockHardness(world, pos) > 0.0f)
        {
            // Fast mode uses double the durability
            int dmg = (this.isFastMode(stack) ? 2 : 1);

            this.addToolDamage(stack, dmg, livingBase, livingBase);
            return true;
        }

        return false;
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
        return enchantment.type == EnumEnchantmentType.ALL ||
               enchantment.type == EnumEnchantmentType.BREAKABLE ||
               enchantment.type == EnumEnchantmentType.DIGGER;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase living1, EntityLivingBase living2)
    {
        this.addToolDamage(stack, 2, living1, living2);
        return true;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player)
    {
        //System.out.printf("onBlockStartBreak() @ %s\n", player.getEntityWorld().isRemote ? "client" : "server");
        World world = player.getEntityWorld();
        IBlockState state = world.getBlockState(pos);

        if (state.getBlockHardness(world, pos) >= 0f)
        {
            if (world.isRemote == false)
            {
                BlockUtils.setBlockToAirWithoutSpillingContents(world, pos);
            }

            if (player.capabilities.isCreativeMode == false)
            {
                this.addToolDamage(stack, world, pos, state, player);
            }

            return world.isRemote == false;
        }

        return false;
    }

    @Override
    public float getStrVsBlock(ItemStack stack, IBlockState state)
    {
        if (this.isToolBroken(stack))
        {
            return 0.1f;
        }

        float eff = this.efficiencyOnProperMaterial;
        // 34 is the minimum to allow instant mining with just Efficiency V (= no beacon/haste) on cobble,
        // 124 is the minimum for iron blocks @ hardness 5.0f (which is about the highest of "normal" blocks), 1474 on obsidian.
        // So maybe around 160 might be ok? I don't want insta-mining on obsidian, but all other types of "rock".
        if (this.isFastMode(stack))
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

        //if (ForgeHooks.isToolEffective(world, pos, stack))
        for (String type : this.getToolClasses(stack))
        {
            if (state.getBlock().isToolEffective(type, state))
            {
                //System.out.printf("getStrVsBlock(); isToolEffective() true, eff: %.3f\n ", eff);
                return eff;
            }
        }

        if (this.canHarvestBlock(state, stack))
        {
            //System.out.printf("getStrVsBlock(); canHarvestBlock() true, eff: %.3f\n ", eff);
            return eff;
        }

        //System.out.printf("getStrVsBlock(); not effective: eff: %f\n", super.getStrVsBlock(stack, state));
        return super.getStrVsBlock(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack)
    {
        if (this.isToolBroken(stack))
        {
            return false;
        }

        //System.out.println("canHarvestBlock(): false");
        return state.getMaterial() != Material.BARRIER;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
    {
        if (this.isToolBroken(stack) == false)
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
        double dmg = this.isToolBroken(stack) ? 0.5f : 6f; // Default to almost no damage if the tool is broken

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND)
        {
            String modifierName = "Tool modifier";

            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, modifierName, dmg, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, modifierName, -2.7f, 0));
        }

        return multimap;
    }

    private boolean isFastMode(ItemStack stack)
    {
        return NBTUtils.getBoolean(stack, null, "FastMode");
    }

    @Override
    public void doKeyBindingAction(EntityPlayer player, ItemStack stack, int key)
    {
        // Just Toggle mode key: Change the dig mode
        if (key == HotKeys.KEYBIND_ID_TOGGLE_MODE)
        {
            NBTUtils.cycleByteValue(stack, null, "FastMode", 1);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addTooltipLines(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        String rst = TextFormatting.RESET.toString() + TextFormatting.GRAY.toString();
        String preGreen = TextFormatting.GREEN.toString();
        String modeStr;

        if (this.isFastMode(stack))
        {
            modeStr = I18n.format("enderutilities.tooltip.item.fast");
        }
        else
        {
            modeStr = I18n.format("enderutilities.tooltip.item.normal");
        }

        list.add(I18n.format("enderutilities.tooltip.item.mode") + ": " + preGreen + modeStr + rst);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean hasEffect(ItemStack stack)
    {
        return false;
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "fastmode"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemVoidPickaxe.this.isFastMode(stack) ? 1.0F : 0.0F;
            }
        });
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "broken"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemVoidPickaxe.this.isToolBroken(stack) ? 1.0F : 0.0F;
            }
        });
    }
}
