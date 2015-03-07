package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IChargeable;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.EUStringUtils;

public class ItemEnderCapacitor extends ItemEnderUtilities implements IChargeable, IModule
{
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
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return super.getUnlocalizedName() + "." + stack.getItemDamage();
        }

        return super.getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        for (int i = 0; i <= 2; i++)
        {
            list.add(new ItemStack(this, 1, i));

            // Add a fully charged version for creative tab and NEI
            ItemStack tmp = new ItemStack(this, 1, i);
            this.addCharge(tmp, this.getCapacityFromItemType(tmp), true);
            list.add(tmp);
        }
    }

    public int getCapacityFromItemType(ItemStack stack)
    {
        if (stack.getItemDamage() == 0) { return 10000; } // Basic
        if (stack.getItemDamage() == 1) { return 100000; } // Enhanced
        if (stack.getItemDamage() == 2) { return 500000; } // Advanced
        return 10000; // Basic
    }

    @Override
    public int getCapacity(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || nbt.hasKey("EnderChargeCapacity", Constants.NBT.TAG_INT) == false)
        {
            return this.getCapacityFromItemType(stack);
        }

        return nbt.getInteger("EnderChargeCapacity");
    }

    @Override
    public void setCapacity(ItemStack stack, int capacity)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            nbt = new NBTTagCompound();
        }

        nbt.setInteger("EnderChargeCapacity", capacity);
        stack.setTagCompound(nbt);
    }

    @Override
    public int getCharge(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null)
        {
            return 0;
        }

        return nbt.getInteger("EnderChargeAmount");
    }

    @Override
    public int addCharge(ItemStack stack, int amount, boolean doCharge)
    {
        int charge = this.getCharge(stack);
        int capacity = this.getCapacity(stack);

        if ((capacity - charge) < amount)
        {
            amount = (capacity - charge);
        }

        if (doCharge == true)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                nbt = new NBTTagCompound();
            }

            nbt.setInteger("EnderChargeAmount", charge + amount);
            stack.setTagCompound(nbt);
        }

        return amount;
    }

    @Override
    public int useCharge(ItemStack stack, int amount, boolean doUse)
    {
        int charge = this.getCharge(stack);

        if (charge < amount)
        {
            amount = charge;
        }

        if (doUse == true)
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if (nbt == null)
            {
                nbt = new NBTTagCompound();
            }

            nbt.setInteger("EnderChargeAmount", charge - amount);
            stack.setTagCompound(nbt);
        }

        return amount;
    }

    @Override
    public int useCharge(ItemStack stack, EntityPlayer player, int amount, boolean doUse)
    {
        // Don't use or require charge in creative mode
        if (player.capabilities.isCreativeMode == true)
        {
            return amount;
        }

        return this.useCharge(stack, amount, doUse);
    }

    @Override
    public void addInformationSelective(ItemStack stack, EntityPlayer player, List<String> list, boolean advancedTooltips, boolean verbose)
    {
        int charge = this.getCharge(stack);
        int capacity = this.getCapacity(stack);

        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberWithKSeparators(charge) + " / " + EUStringUtils.formatNumberWithKSeparators(capacity));
        /*if (EnderUtilities.proxy.isShiftKeyDown() == true)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberWithKSeparators(charge) + " / " + EUStringUtils.formatNumberWithKSeparators(capacity));
        }
        else
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.charge") + ": " + EUStringUtils.formatNumberFloorWithPostfix(charge) + " / " + EUStringUtils.formatNumberFloorWithPostfix(capacity));
        }*/
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return ModuleType.TYPE_ENDERCAPACITOR;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return stack.getItemDamage();
        }

        return -1;
    }

    @SideOnly(Side.CLIENT)
    public void registerTextures(TextureMap textureMap)
    {
        this.textures = new TextureAtlasSprite[6];
        for (int i = 0; i < 3; ++i)
        {
            this.registerTexture(i    , this.name + ".empty."   + i, textureMap);
            this.registerTexture(i + 3, this.name + ".charged." + i, textureMap);
        }
    }

    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getItemTexture(ItemStack stack)
    {
        int index = stack.getItemDamage();

        if (index >= 0 && index <= 2)
        {
            if (this.getCharge(stack) > 0)
            {
                index += 3;
            }
        }

        return this.textures[index < this.textures.length ? index : 0];
    }
}
