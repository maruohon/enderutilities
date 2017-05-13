package fi.dy.masa.enderutilities.item.part;

import java.util.List;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EUStringUtils;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemEnderCapacitor extends ItemEnderUtilities implements IChargeable, IModule
{
    public static final int CHARGE_RATE_FROM_ENERGY_BRIDGE = 100;

    public ItemEnderCapacitor()
    {
        super();
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART_ENDERCAPACITOR);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage 0: Ender Capacitor (Basic)
        // Damage 1: Ender Capacitor (Enhanced)
        // Damage 2: Ender Capacitor (Advanced)
        // Damage 3: Ender Capacitor (Creative)
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 3)
        {
            return super.getUnlocalizedName() + "_" + stack.getMetadata();
        }

        return super.getUnlocalizedName();
    }

    public int getCapacityFromItemType(ItemStack stack)
    {
        int dmg = stack.getMetadata();
        if (dmg == 0) { return 10000; } // Basic
        if (dmg == 1) { return 100000; } // Enhanced
        if (dmg == 2) { return 500000; } // Advanced
        if (dmg == 3) { return 1000000000; } // Creative
        return 10000; // Basic
    }

    private int getCapacity(ItemStack stack, NBTTagCompound nbt)
    {
        if (nbt == null || nbt.hasKey("EnderChargeCapacity", Constants.NBT.TAG_INT) == false)
        {
            return this.getCapacityFromItemType(stack);
        }

        return nbt.getInteger("EnderChargeCapacity");
    }

    @Override
    public int getCapacity(ItemStack stack)
    {
        return this.getCapacity(stack, stack.getTagCompound());
    }

    @Override
    public void setCapacity(ItemStack stack, int capacity)
    {
        NBTUtils.setInteger(stack, null, "EnderChargeCapacity", capacity);
    }

    private int getCharge(NBTTagCompound nbt)
    {
        return nbt.getInteger("EnderChargeAmount");
    }

    @Override
    public int getCharge(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null ? this.getCharge(nbt) : 0;
    }

    private void setCharge(NBTTagCompound nbt, int value)
    {
        nbt.setInteger("EnderChargeAmount", value);
    }

    @Override
    public int addCharge(ItemStack stack, int amount, boolean doCharge)
    {
       // Creative capacitor, don't allow actually re-charging (but accept infinite charge)
        if (stack.getMetadata() == 3)
        {
            return amount;
        }

        NBTTagCompound nbt = NBTUtils.getCompoundTag(stack, null, true);

        int charge = this.getCharge(nbt);
        int capacity = this.getCapacity(stack, nbt);

        if ((capacity - charge) < amount)
        {
            amount = (capacity - charge);
        }

        if (doCharge)
        {
            this.setCharge(nbt, charge + amount);
        }

        return amount;
    }

    @Override
    public int useCharge(ItemStack stack, int amount, boolean doUse)
    {
        // Creative capacitor, allow using however much is charge is requested, unless this is a completely empty capacitor
        if (stack.getMetadata() == 3 && this.getCharge(stack) > 0)
        {
            return amount;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || amount < 0)
        {
            return 0;
        }

        int charge = this.getCharge(nbt);

        if (charge < amount)
        {
            amount = charge;
        }

        if (doUse)
        {
            this.setCharge(nbt, charge - amount);
        }

        return amount;
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        int charge = this.getCharge(stack);
        int capacity = this.getCapacity(stack);

        list.add(I18n.format("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberWithKSeparators(charge) + " / " + EUStringUtils.formatNumberWithKSeparators(capacity));
        /*if (EnderUtilities.proxy.isShiftKeyDown())
        {
            list.add(I18n.format("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberWithKSeparators(charge) + " / " + EUStringUtils.formatNumberWithKSeparators(capacity));
        }
        else
        {
            list.add(I18n.format("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberFloorWithPostfix(charge) + " / " + EUStringUtils.formatNumberFloorWithPostfix(capacity));
        }*/
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 3)
        {
            return ModuleType.TYPE_ENDERCAPACITOR;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 3)
        {
            return stack.getMetadata();
        }

        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        for (int i = 0; i < 4; i++)
        {
            list.add(new ItemStack(item, 1, i));

            // Add a fully charged version for creative tab
            ItemStack tmp = new ItemStack(item, 1, i);
            tmp.setTagCompound(new NBTTagCompound());
            this.setCharge(tmp.getTagCompound(), this.getCapacityFromItemType(tmp));
            list.add(tmp);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "tex=empty_0"),
                new ModelResourceLocation(rl, "tex=empty_1"),
                new ModelResourceLocation(rl, "tex=empty_2"),
                new ModelResourceLocation(rl, "tex=empty_3"),
                new ModelResourceLocation(rl, "tex=charged_0"),
                new ModelResourceLocation(rl, "tex=charged_1"),
                new ModelResourceLocation(rl, "tex=charged_2"),
                new ModelResourceLocation(rl, "tex=charged_3")
        };
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack)
    {
        String pre = this.getCharge(stack) > 0 ? "tex=charged_" : "tex=empty_";
        int index = MathHelper.clamp(stack.getMetadata(), 0, 3);

        return new ModelResourceLocation(Reference.MOD_ID + ":" + "item_" + this.name, pre + index);
    }
}
