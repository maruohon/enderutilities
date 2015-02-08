package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.ItemModule;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class ItemEnderPart extends ItemModule
{
    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemEnderPart()
    {
        super();
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage 0: Ender Alloy (Basic)
        // Damage 1: Ender Alloy (Enhanced)
        // Damage 2: Ender Alloy (Advanced)
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + stack.getItemDamage();
        }

        // Damage 10: Inactive Ender Core (Basic)
        // Damage 11: Inactive Ender Core (Enhanced)
        // Damage 12: Inactive Ender Core (Advanced)
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (stack.getItemDamage() - 10) + ".inactive";
        }

        // Damage 15: Ender Core (Basic)
        // Damage 16: Ender Core (Enhanced)
        // Damage 17: Ender Core (Advanced)
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + (stack.getItemDamage() - 15) + ".active";
        }

        // Damage 20: Ender Stick
        if (stack.getItemDamage() == 20)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK;
        }

        // Damage 21: Ender Rope
        if (stack.getItemDamage() == 21)
        {
            return super.getUnlocalizedName() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE;
        }

        return super.getUnlocalizedName();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List list)
    {
        if (Configs.disableItemCraftingPart.getBoolean(false) == false)
        {
            // Ender Alloys
            for (int i = 0; i <= 2; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            // Inactive Ender Cores
            for (int i = 10; i <= 12; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            // (Active) Ender Cores
            for (int i = 15; i <= 17; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }

            list.add(new ItemStack(this, 1, 20)); // Ender Stick
            list.add(new ItemStack(this, 1, 21)); // Ender Rope
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage)
    {
        // Ender Alloy
        if (damage >= 0 && damage <= 2) { return this.iconArray[damage]; }

        // Inactive Ender Core
        if (damage >= 10 && damage <= 12) { return this.iconArray[damage - 7]; }

        // Ender Core (active)
        if (damage >= 15 && damage <= 17) { return this.iconArray[damage - 9]; }

        // Ender Stick
        if (damage == 20) { return this.iconArray[9]; }

        // Ender Rope
        if (damage == 21) { return this.iconArray[10]; }

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + ".0");
        this.iconArray = new IIcon[11];

        int i = 0, j;

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERALLOY + "." + j);
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".inactive");
        }

        for (j = 0; j < 3; ++i, ++j)
        {
            this.iconArray[i] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERCORE + "." + j + ".active");
        }

        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERSTICK);
        this.iconArray[i++] = iconRegister.registerIcon(this.getIconString() + "." + ReferenceNames.NAME_ITEM_ENDERPART_ENDERROPE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.craftingingredient"));

        // Damage 10: Inactive Ender Core (Basic)
        // Damage 11: Inactive Ender Core (Enhanced)
        // Damage 12: Inactive Ender Core (Advanced)
        if (stack.getItemDamage() >= 10 && stack.getItemDamage() <= 12)
        {
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.rightclick.endercrystal.to.activate.1"));
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.rightclick.endercrystal.to.activate.2"));
            list.add(StatCollector.translateToLocal("enderutilities.tooltip.item.rightclick.endercrystal.to.activate.3"));
        }
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        // Active Ender Cores
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return ModuleType.TYPE_ENDERCORE_ACTIVE;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        // Only Active Ender Cores are modules
        if (stack.getItemDamage() >= 15 && stack.getItemDamage() <= 17)
        {
            return stack.getItemDamage() - 15;
        }

        return 0;
    }
}
