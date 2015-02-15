package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;

public class ItemLinkCrystal extends ItemLocationBound implements IModule
{
    public static final int TYPE_LOCATION = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_PORTAL = 2;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconArray;

    public ItemLinkCrystal()
    {
        super();
        this.setMaxStackSize(64);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDERPART_LINKCRYSTAL);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        // Damage 0: Link Crystal (In-World)
        // Damage 1: Link Crystal (Inventory)
        // Damage 2: Link Crystal (Portal)
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
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            // FIXME Disabled the Portal type Link Crystal until it is actually used
            for (int i = 0; i <= 1; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int damage)
    {
        if (damage >= 0 && damage <= 2)
        {
            return this.iconArray[damage];
        }

        return this.itemIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon(this.getIconString() + ".location");
        this.iconArray = new IIcon[3];

        this.iconArray[0] = iconRegister.registerIcon(this.getIconString() + ".location");
        this.iconArray[1] = iconRegister.registerIcon(this.getIconString() + ".block");
        this.iconArray[2] = iconRegister.registerIcon(this.getIconString() + ".portal");
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2)
        {
            return ModuleType.TYPE_LINKCRYSTAL;
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

        return 0;
    }
}
