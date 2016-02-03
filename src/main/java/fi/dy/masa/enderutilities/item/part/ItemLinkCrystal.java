package fi.dy.masa.enderutilities.item.part;

import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.setup.Configs;
import fi.dy.masa.enderutilities.util.TooltipHelper;
import fi.dy.masa.enderutilities.util.nbt.NBTHelperTarget;

public class ItemLinkCrystal extends ItemLocationBound implements IModule
{
    public static final int TYPE_LOCATION = 0;
    public static final int TYPE_BLOCK = 1;
    public static final int TYPE_PORTAL = 2;

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

    @Override
    public String getTargetDisplayName(ItemStack stack)
    {
        NBTHelperTarget target = NBTHelperTarget.getTargetFromItem(stack);
        if (target != null)
        {
            // Display the target block name if it's a Block type Link Crystal
            if (this.getModuleTier(stack) == ItemLinkCrystal.TYPE_BLOCK)
            {
                return NBTHelperTarget.getTargetBlockDisplayName(target);
            }
            // Location type Link Crystal
            else if (this.getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION)
            {
                return TooltipHelper.getDimensionName(target.dimension, target.dimensionName, true);
            }
        }

        return null;
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

        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> list)
    {
        if (Configs.disableItemLinkCrystal.getBoolean(false) == false)
        {
            // FIXME Disabled the Portal type Link Crystal until it is actually used
            for (int i = 0; i < 2; i++)
            {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public ResourceLocation[] getItemVariants()
    {
        String rl = Reference.MOD_ID + ":" + "item_" + this.name;

        return new ResourceLocation[] {
                new ModelResourceLocation(rl, "type=location"),
                new ModelResourceLocation(rl, "type=block"),
                new ModelResourceLocation(rl, "type=portal")
        };
    }
}
