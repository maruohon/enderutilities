package fi.dy.masa.enderutilities.item;

import net.minecraft.item.ItemStack;
import fi.dy.masa.enderutilities.item.base.ItemLocationBoundModular;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class ItemEnderLasso extends ItemLocationBoundModular
{
    public static final int ENDER_CHARGE_COST = 2000;

    public ItemEnderLasso()
    {
        super();
        this.setMaxStackSize(1);
        this.setMaxDamage(0);
        this.setUnlocalizedName(ReferenceNames.NAME_ITEM_ENDER_LASSO);
    }

    /* Returns the maximum number of modules that can be installed on this item. */
    @Override
    public int getMaxModules(ItemStack stack)
    {
        return 4;
    }
}
