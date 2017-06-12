package fi.dy.masa.enderutilities.item.part;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.item.base.IModule;
import fi.dy.masa.enderutilities.item.base.ItemLocationBound;
import fi.dy.masa.enderutilities.item.base.ItemModule.ModuleType;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.reference.ReferenceNames;
import fi.dy.masa.enderutilities.util.nbt.TargetData;

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
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return super.getUnlocalizedName() + "_" + stack.getMetadata();
        }

        return super.getUnlocalizedName();
    }

    @Override
    public String getTargetDisplayName(ItemStack stack)
    {
        // Location type Link Crystal
        if (this.getModuleTier(stack) == ItemLinkCrystal.TYPE_LOCATION)
        {
            TargetData target = TargetData.getTargetFromItem(stack);
            return target != null ? target.getDimensionName(true) : null;
        }

        return super.getTargetDisplayName(stack);
    }

    @Override
    public ModuleType getModuleType(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return ModuleType.TYPE_LINKCRYSTAL;
        }

        return ModuleType.TYPE_INVALID;
    }

    @Override
    public int getModuleTier(ItemStack stack)
    {
        if (stack.getMetadata() >= 0 && stack.getMetadata() <= 2)
        {
            return stack.getMetadata();
        }

        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> list)
    {
        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @SideOnly(Side.CLIENT)
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
